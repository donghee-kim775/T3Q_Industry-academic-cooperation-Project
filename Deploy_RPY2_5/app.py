from flask import Flask, jsonify, request
import pandas as pd
import json
import source_code

app = Flask(__name__)

@app.route("/hellotest", methods=['GET'])
def test():
   print("hello world")

@app.route("/api7", methods=['POST'])
def sign_up():
   value = request.get_json()
   print(value)
   response = {}

   try:
      resdict, code, msg = source_code.runScript(value)
      response['result'] = resdict
      response['code'] = code
      response['msg'] = msg
   except:
      code = "999"
      msg = "정의되지 않은 error입니다."
      response["code"]=code
      response["msg"]=msg
   return response