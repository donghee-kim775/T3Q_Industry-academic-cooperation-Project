#-*- coding: utf-8 -*-
import pandas as pd
import rpy2.robjects as ro
from rpy2.robjects import r
from rpy2.robjects import pandas2ri
from rpy2.robjects.conversion import localconverter

def train(tm):
    pass

def init_svc(im):
    pass

def DataProcessing(value):
    origin_data = value['data']

    # experiment data 전처리
    experiment_data = origin_data['experiment data']

    experiment_data = pd.DataFrame(experiment_data)
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

    return experiment_data, input_df

def runScript(value):
    experiment_data, input_df  = DataProcessing(value)
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

        #############
        ##-effects-##
        #############
        y_list = value['data']['header']

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

        ##############
        ##-imageenc-##
        ##############
        # 반환값
        image_df = ro.conversion.rpy2py(ro.globalenv['image_df'])
        
        image_result = {}
        for image_type in image_df['image_type'].unique():
            subset = image_df[image_df['image_type'] == image_type]
            image_result[image_type] = {
                'image_name': subset['image_name'].values[0],
                'image_to_base64': subset['image_to_text'].values[0]
            }

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
    return res_dict, code, msg