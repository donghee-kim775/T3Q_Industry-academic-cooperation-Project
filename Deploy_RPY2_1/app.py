from flask import Flask, jsonify, request
import json

app = Flask(__name__)

@app.route('/')
def home():
    return "Welcome to the Flask API with RPY2!"