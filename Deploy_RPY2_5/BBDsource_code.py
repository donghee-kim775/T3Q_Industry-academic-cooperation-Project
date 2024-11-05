#-*- coding: utf-8 -*-
import pandas as pd
import rpy2.robjects as ro
from rpy2.robjects import r
from rpy2.robjects import pandas2ri
from rpy2.robjects.conversion import localconverter
import re

def train(tm):
    pass

def init_svc(im):
    pass

def DataProcessing(value):
    print("### data 전처리 시작 ###")
    origin_data = value['data'][0][0]
    
    print("### origin_data ###")
    input = origin_data['formulation']
    print(input)
    
    print("### experiment data ###")
    experiment_data = origin_data['experiment data']
    
    try :
        #################################################
        ################ input_df 전처리 ################
        #################################################
        print("#### input_df 전처리 시작 ####")
        for i in range(len(input)):
            origin_excipients = input[i]['excipients']
            blank_remove_excipients = origin_excipients.replace(" ", ".")
            sign_remove_excipients = re.sub(r"[^\uAC00-\uD7A30-9a-zA-Z\s]", ".", blank_remove_excipients)
            input[i]['excipients'] = sign_remove_excipients

        excipients = [item['excipients'] for item in input]
        input_range_min = [item['input range']['min'] for item in input]
        input_range_max = [item['input range']['max'] for item in input]

        input_df = pd.DataFrame({
            'excipients': excipients,
            'input.range.min': input_range_min,
            'input.range.max': input_range_max
        })
        
        print(input_df)
        
        ######################################################
        ################ experiment_df 전처리 ################
        ######################################################
        experiment_data = origin_data['experiment data']
        df_data = {}
        
        for item in experiment_data:
            for key, value in item.items():
                df_data[key] = value

        experiment_data = pd.DataFrame(df_data)
        experiment_data = experiment_data.astype(int)
        print(experiment_data)
        
    except Exception as e:
        print(e)
        
    print("### 데이터 최종 전처리 결과 ###")
    print(experiment_data)
    print(input_df)
    
    return experiment_data, input_df

def runScript(value):
    try:
        experiment_data, input_df  = DataProcessing(value)
        origin_data =value['data'][0][0]
        
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
            print(r.source("./Rscript/BBDGraph.R"))

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
            
            print("###############################")
            print("##### Python effect_list #####")
            print("###############################")
            print(effect_list)
            
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
            
            print("###############################")
            print("##### Python image_result #####")
            print("###############################")
            print(image_result)    
    except Exception as e:
        print("error : " , e)
        
    ##############
    ##-return값-##
    ##############
    res_dict = {}
    res_dict['contour'] = image_result['contour']
    res_dict['response'] = image_result['response']
    res_dict['pareto'] = image_result['pareto']
    res_dict['effects'] = effect_list
    code = "000"
    msg = "success"
    
    print("###########################")
    print("##### Python res_dict #####")
    print("###########################")
    print(res_dict)
    
    return res_dict, code, msg 