from flask import Flask, jsonify, request
import pandas as pd
import json
import CCDDOE, BBDDOE, SLDDOE
import CCDsource_code, BBDsource_code

app = Flask(__name__)

@app.route("/hellotest", methods=['GET'])
def test():
   print("hello world")

#################################################
###################### CCD ######################
#################################################
@app.route("/CCD_api5", methods=['POST'])
def CCD_DOE():
   print("#########rquest#########")
   value = request.get_json()
   print(value)
   response = {}
   
   try:
      resdict, code, msg = CCDDOE.runScript(value)
      print("#### app.py CCDDOE response ####")
      print(resdict)
      print(code)
      print(msg)
      print("#### app.py CCDDOE response ####")
      
      response['result'] = resdict
      response['code'] = code
      response['msg'] = msg
      
      print(response)
   except Exception as e:
      print(e)
      response['code'] = "999"
      response['msg'] = "정의되지 않은 error입니다."

   return response
   
@app.route("/CCD_api6", methods=['POST'])
def CCD():
   print("#########rquest#########")
   value = request.get_json()
   print(value)
   response = {}

   try:
      resdict, code, msg = CCDsource_code.runScript(value)
      response['result'] = resdict
      response['code'] = code
      response['msg'] = msg
   except:
      response["code"]="999"
      response["msg"]="정의되지 않은 error입니다."
      
   print(response)
   return response

#################################################
###################### BBD ######################
#################################################
@app.route("/BBD_api5", methods = ['POST'])
def BBD_DOE():
   print("#########rquest#########")
   value = request.get_json()
   print(value)
   response = {}
   
   try:
      resdict, code, msg = BBDDOE.runScript(value)
      print("#### app.py BBDDOE response ####")
      print(resdict)
      print(code)
      print(msg)
      print("#### app.py BBDDOE response ####")
      
      response['result'] = resdict
      response['code'] = code
      response['msg'] = msg
      
      print(response)
   except Exception as e:
      print("error : ", e)
      response['code'] = "999"
      response['msg'] = "정의되지 않은 error입니다."

   return response

   
@app.route("/BBD_api6", methods = ['POST'])
def BBD():
   print("#########rquest#########")
   value = request.get_json()
   print(value)
   response = {}

   try:
      resdict, code, msg = BBDsource_code.runScript(value)
      response['result'] = resdict
      response['code'] = code
      response['msg'] = msg
   except Exception as e:
      print(e)
      response["code"]="999"
      response["msg"]="정의되지 않은 error입니다."
      
   print(response)
   return response
   

#################################################
###################### SLD ######################
#################################################
@app.route("/SLD_api5", methods = ['POST'])
def SLD_DOE():
   print("#########rquest#########")
   value = request.get_json()
   print(value)
   response = {}
   
   try:
      resdict, code, msg = SLDDOE.runScript(value)
      print("#### app.py SLDDOE response ####")
      response['result'] = resdict
      response['code'] = code
      response['msg'] = msg
      print("#### app.py SLDDOE response ####")
      
      print(response)
   except Exception as e:
      print(e)
      response['code'] = "999"
      response['msg'] = "정의되지 않은 error입니다."
   
   return response
   
# @app.route("/SLD_api6", methods = ['POST'])
# def SLD():
   
