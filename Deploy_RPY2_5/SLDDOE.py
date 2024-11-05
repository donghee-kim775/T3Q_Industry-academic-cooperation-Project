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
    print("###data 전처리 시작###")
    origin_data = value['data'][0][0]
    print("## input ##")
    
    input = origin_data['formulation']
    print(input)
    
    try:
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
        
        #####################################################
        ################ header + cqa 전처리 ################
        #####################################################
        print("#### header + cqa 전처리 시작 ####")
        formulation = origin_data['formulation']
        cqas = origin_data['CQAs']
        
        excipients_header = []
        cqa_header = []
        header = []
        
        for i in range(len(formulation)):
            export_dict = formulation[i]
            excipients = export_dict.get("excipients")
            excipients_header.append(excipients)
            
        for j in range(len(cqas)):
            cqa = cqas[j]
            cqa_header.append(cqa)
            
        header = excipients_header + cqa_header
        
        return input_df, excipients_header, header
    
    except Exception as e:
        print(e)
        
def runScript(value):
    print("###### runScript ######")
    SLD_DOE_Script = "./Rscript/SLDDoE.R"
    try:
        input_df, excipients_header, header_result  = DataProcessing(value)
        with localconverter(ro.default_converter + pandas2ri.converter):
            pandas2ri.activate()

            input_data_r = ro.conversion.py2rpy(input_df)
            ro.r.assign("data", input_data_r)
            
            r = ro.r
            print(r.source(SLD_DOE_Script))
            
            #############
            ## SLD_DOE ##
            #############
            SLD_DF = ro.conversion.rpy2py(ro.globalenv['df.sld_DoE'])
            
        ###################
        ## Make Response ##
        ###################
        experiment_dict = {}
        experiment_list = []    
            
        res_dict = {}
        res_dict["header"] = header_result
        res_dict["experiment_data"] = experiment_list
        code = "000"
        msg = "success"
        
        return res_dict, code, msg
    
    except Exception as e:
        print(e)