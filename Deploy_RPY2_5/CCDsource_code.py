#-*- coding: utf-8 -*-
import pandas as pd
import rpy2.robjects as ro
from rpy2.robjects import r
from rpy2.robjects import pandas2ri
from rpy2.robjects.conversion import localconverter
import json
import re

def train(tm):
    pass

def init_svc(im):
    pass

'''
data 전처리
'''
def DataProcessing(value):
    # 공백 처리
    for data in value['data'][0][0]['experiment data']:
        for key in list(data.keys()):
            new_key = key.replace(' ', '.')
            data[new_key] = data.pop(key)
        
    value['data'][0][0]['header'] = [header.replace(' ', '.') for header in value['data'][0][0]['header']]
    
    print("###data 전처리 시작###")
    origin_data = value['data'][0][0]
    
    print("## origin_data ##")
    print(origin_data)
    try :
        experiment_data = origin_data['experiment data']
        
        df_data = {}
        for item in experiment_data:
            for key, value in item.items():
                df_data[key] = value

        experiment_data = pd.DataFrame(df_data)
        experiment_data = experiment_data.astype(int)
    

        # formulation data 전처리
        input = origin_data['formulation']

        # 딕셔너리 리스트를 DataFrame으로 변환
        input_df = pd.DataFrame(input)
        
        # excipients replace(공백 -> .) : R에서는 공백 있을 시 error
        input_df['excipients'] = input_df['excipients'].apply(lambda x: x.replace(" ", "."))

        input_df['input.range.min'] = input_df['input range'].apply(lambda x: x['min'])
        input_df['input.range.max'] = input_df['input range'].apply(lambda x: x['max'])

        input_df = input_df.drop(columns = ['kind', 'max', 'use range', 'input range'])

        input_df['input.range.min'] = input_df['input.range.min'].astype(int)
        input_df['input.range.max'] = input_df['input.range.max'].astype(int)
    except Exception as e:
        print(e)
        
    print("### 데이터 최종 전처리 결과 ###")
    print(experiment_data)
    print(input_df)
    
    return experiment_data, input_df

'''
anova 수치를 JSON 형식으로 변환
'''
def anova_df_to_list(df):
    # NA 값을 0으로 대체
    df = df.fillna(0)

    # 데이터프레임을 리스트 형식으로 변환
    anova_list = []
    
    for response_type, group in df.groupby("response_type"):
        sources = {}
        for _, row in group.iterrows():
            print(row)
            print(row["source"])
            row["source"] = re.sub(r"\s*\(.*\)", "", row["source"])
            sources[row["source"]] = {
                "df": row["DF"],
                "sum_sq": row["sumsq"],
                "mean_sq": row["meansq"],
                "F value": row["F_VALUE"],
                "Pr(>F)": row["PR(>F)"]
            }
        anova_list.append({
            "response_type": response_type,
            "source": sources
        })

    return anova_list

'''
CCD 실행
'''
def runScript(value):
    try:
        experiment_data, input_df  = DataProcessing(value)
        origin_data = value['data'][0][0]
        with localconverter(ro.default_converter + pandas2ri.converter):
            pandas2ri.activate()

            input_data_r = ro.conversion.py2rpy(input_df)
            experiment_data_r = ro.conversion.py2rpy(experiment_data)

            ro.r.assign("data", input_data_r)
            ro.r.assign("df.response", experiment_data_r)
            
            ################
            #-Graph.R 실행-#
            ################
            r = ro.r
            print(r.source('./Rscript/CCDGraph.R'))
            print("Return Python")
            #############
            ##-effects-##
            #############
            y_list = origin_data['header']

            print("y_list : ", y_list)
            # min max 추출
            range_list = []
            for i in range(len(y_list)):
                y_value = y_list[i]
                convert_y_list = experiment_data[y_value].tolist()

                # min값 y값 추출
                min_value = min(convert_y_list)
                max_value = max(convert_y_list)

                # min / max -> int? float?
                # float -> 소수 세번째 자리
                ## min ##
                if isinstance(min_value, int):
                    pass
                else:
                    min_value = round(min_value, 2)
                ## max ##
                if isinstance(max_value, int):
                    pass
                else:
                    min_value = round(max_value, 2)

                range_dict = {}
                range_dict['min'] = min_value
                range_dict['max'] = max_value

                range_list.append(range_dict)

            # CQA
            range_str_list = []
            
            # range value
            result_range_list = []

            for z in range(len(y_list)):
                range_str_list.append(y_list[z])
                result_range_list.append(range_list[z])
                effect_range_dict = dict(zip(range_str_list, result_range_list))
            
            effect_list = []
            effect_range_dict = list(effect_range_dict.items())
            for f in range(len(y_list)):
                new_dict = {}
                y_str = "Y%d" % (f+1)
                new_dict[y_str] = {effect_range_dict[f][0] : effect_range_dict[f][1]}
                effect_list.append(new_dict)

            ###############
            ###- anova -###
            ###############
            print("### anova ###")
            final_anova_df = ro.conversion.rpy2py(ro.globalenv['final_anova_df'])
            anova_list = anova_df_to_list(final_anova_df)

            ##############
            ##-imageenc-##
            ##############
            # 반환값
            image_df = ro.conversion.rpy2py(ro.globalenv['image_df']) 
            print("###########################")
            print("##### Python image_DF #####")
            print("###########################")
            print(image_df)
            
            image_result = {}
            
            for _, row in image_df.iterrows():
                image_type = row['image_type']
                
                if image_type not in image_result:
                    image_result[image_type] = []
                    
                image_result[image_type].append({
                    'image_name': row['image_name'],
                    'image_to_base64': row['image_to_text']
                })
            
            print("##########################")    
            print("###### image_result ######")
            print("##########################")
            print(image_result)
            
    except Exception as e:
        print('error :', e)
    
    ##############
    ##-return값-##
    ##############
    res_dict = {}
    res_dict['contour'] = image_result['contour']
    res_dict['response'] = image_result['response']
    res_dict['pareto'] = image_result['pareto']
    res_dict['anova'] = anova_list
    res_dict['effects'] = effect_list
    code = "000"
    msg = "success"
    
    print("###########################")
    print("##### Python res_dict #####")
    print("###########################")
    print(res_dict)
    
    return res_dict, code, msg