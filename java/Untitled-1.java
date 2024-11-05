@RequestMapping(value = "/pharmai/formulation/getApi6Ajax.do")
public @ResponseBody DataMap getApi6Ajax(HttpServletRequest request, HttpServletResponse response, ModelMap model)
        throws Exception {
    System.out.println("#########getApi6Ajax.do###########");
    // return 상태
    DataMap resultStats = new DataMap();

    log.debug("####" + this.getClass().getName() + " START ####");
    DataMap resultJSON = new DataMap();
    DataMap param = RequestUtil.getDataMap(request);
    
    System.out.println(param);
    
    URL url;
    HttpURLConnection conn;

    StringBuffer sb = new StringBuffer();
    BufferedReader in = null;
/// ********************************************************
/// ***************** Put Data for Request *****************
/// ********************************************************
    JSONObject jsonob = new JSONObject();
    JSONObject jdata = new JSONObject();
    JSONArray jsonarr = new JSONArray();
    JSONArray jsonArrPrimary = new JSONArray();

    System.out.println("get_api_6_param : " + param);
    
    /// 수정 완료_start
    /// ********************************************
    /// **Request Experiment CSV PATH -> Real Data**------------------------------------------------
    /// ********************************************
    List cqasList = experimentService.selectListFormulaStp_04(param);
    
    DataMap cqasMap = null;
    
    JSONArray cqadata = new JSONArray();
    for (Object list : cqasList) {
        cqasMap = (DataMap) list;
        if (cqasMap.getString("CHECK_YN").equals("Y")) {
            cqadata.add(cqasMap.getString("CQA_NM"));
        }
    }
    
    System.out.println("doecontroller_cqadata : " + cqadata); // header
    System.out.println("cqadatasize : " + cqadata.size()); // cqa개수
    
    List exprDataList = commonMybatisDao.selectList("pharmai.formulation.selectListFormulaStp_05", param);
    int selectStp3TotYn = bioExperimentService.selectStp3TotYn(param);
    
    DataMap data_0 = new DataMap();
    
    JSONArray ExperimentData = new JSONArray();
    JSONArray cqa_data = new JSONArray(); // 최종 cqa 데이터
    
    for(int i = 1+selectStp3TotYn; i <= selectStp3TotYn + cqadata.size(); i++) {
        System.out.println("######## Refac_데이터 #######");
        List<String> datalist = new ArrayList<>(); // cqa value
        String header = ""; // header
        JSONObject cqa_temp = new JSONObject(); // cqa_temp
        
        for(int j = 0; j< exprDataList.size();j++) {
            data_0 = (DataMap) exprDataList.get(j);
            String currentData = data_0.getString("C"+i);
            if (j==0) { // currentData -> header
                System.out.println("#### j = 0 번째 입니다. ####");
                System.out.println(i + "_" + j + "번째 : "+ currentData);
                header = currentData;
            }
            else { // header -> 해당 header 데이터에 나머지 cqa value append
                System.out.println(i + "_" + j + "번째 : "+ currentData);
                datalist.add(currentData);
            }
        }
        cqa_temp.put(header, datalist);
        System.out.println(header + " : " + datalist);
        System.out.println("JSON_temp : " + cqa_temp);
        cqa_data.add(cqa_temp);
    }
    
    jdata.put("experiment data", cqa_data);
    /// ********************************************
    ///-----------------------------------------------------------------------------------------------------
    /// ********************************************		
    /// 수정 완료_end
    List selectStp_03 = experimentService.selectListFormulaStp_03(param);
    List selectStp_04 = experimentService.selectListFormulaStp_04(param);

    DataMap selectStp_03Map = null;
    DataMap selectStp_04Map = null;
    JSONArray selectStp_04List = new JSONArray();
    JSONArray jsonarrFormulation = new JSONArray();

    // API 호출 후 결과를 json 으로 파싱 후 매핑
    JsonNode data = null;

    for (Object list : selectStp_04) {
        selectStp_04Map = (DataMap) list;
        if (selectStp_04Map.getString("CHECK_YN").equals("Y")) {
            selectStp_04List.add(selectStp_04Map.getString("CQA_NM"));
            jdata.put("header", selectStp_04List);
        }
    }

    for (Object list : selectStp_03) {
        selectStp_03Map = (DataMap) list;
        if (selectStp_03Map.getString("CHECK_YN").equals("Y")) {
            JSONObject temp = new JSONObject();
            temp.put("excipients", selectStp_03Map.getString("EXCIPIENT"));
            temp.put("kind", selectStp_03Map.getString("KIND"));

            JSONObject temp1 = new JSONObject();
            temp1.put("value", selectStp_03Map.getString("MAXIMUM"));
            temp1.put("unit", selectStp_03Map.getString("UNIT"));
            temp.put("max", temp1);

            JSONObject temp2 = new JSONObject();
            temp2.put("min", selectStp_03Map.getString("USE_RANGE_S"));
            temp2.put("max", selectStp_03Map.getString("USE_RANGE_E"));
            temp.put("use range", temp2);

            JSONObject temp3 = new JSONObject();
            temp3.put("min", selectStp_03Map.getString("IPT_USE_RANGE_S"));
            temp3.put("max", selectStp_03Map.getString("IPT_USE_RANGE_E"));
            temp.put("input range", temp3);

            jsonarrFormulation.add(temp);

        }
    }
    jdata.put("formulation", jsonarrFormulation);

    /// ***************************************
    /// ************** image-path *************
    /// ***************************************
    jdata.put("pareto-path", param.getString("pareto-path"));
    jdata.put("contour-path", param.getString("contour-path"));
    jdata.put("response-path", param.getString("response-path"));
    
    /// ********************************************
    /// ************** image_dir_write *************
    /// ********************************************
    // add 22.05.27
    String prjct_id = param.getString("prjct_id");
    String pareto_path = EgovPropertiesUtil.getProperty("Globals.fileWebrootPath.formulation") + prjct_id
            + "/api6/tab-1/pareto/";
    String contour_path = EgovPropertiesUtil.getProperty("Globals.fileWebrootPath.formulation") + prjct_id
            + "/api6/tab-2/contour/";
    String response_path = EgovPropertiesUtil.getProperty("Globals.fileWebrootPath.formulation") + prjct_id
            + "/api6/tab-3/response/";
    
    // path에 해당하는 디렉토리가 존재하지 않는 경우 해당 디렉토리 생성
    File directory1 = new File(pareto_path);
    File directory2 = new File(contour_path);
    File directory3 = new File(response_path);

    if (!directory1.exists() || !directory2.exists() || !directory3.exists()) {
        directory1.mkdirs();
        directory2.mkdirs();
        directory3.mkdirs();
    }
    
    /// *********************************************
    /// ***************Complete-Request**************
    /// *********************************************
    jsonarr.add(jdata);
    jsonArrPrimary.add(jsonarr);
    jsonob.put("data", jsonArrPrimary);
    System.out.println("최종 data check : " + jsonob);
    
    
    /// *********************************************
    /// **************API6_Refac-Request*************
    /// *********************************************		
    try {

        url = new URL(EgovPropertiesUtil.getProperty("Globals.api.formulation.api6_Refac"));
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
        bw.write(jsonob.toString());
        bw.flush();
        bw.close();

        in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            sb.append(inputLine.trim());
        }
    /// ********************************************
    /// **-----------------------------------------------
    /// ********************************************
        System.out.println("***** API 요청 완료 *****");
    /// ********************************************
    /// **************Complete-Response*************
    /// ********************************************
        log.debug(sb.toString());
        // JSONParser parser = new JSONParser(); 해당 코드 밑에 코드로 변경
        // data = parser.parse(sb.toString()); 해당 코드 밑에 코드로 변경

        // add 22.06.02 (objectMapper로 데이터 형태 변환)
        ObjectMapper om = new ObjectMapper();
        data = om.readTree(sb.toString());
        
        // output data에 globals.properties env 값 전달
        ((ObjectNode)data).put("system_env", Globals.SYSTEM_ENV);
        
        // data에서 result 값만 가져오기
        JsonNode parse_result = data.get("result");
        
        // testing
        ObjectNode resultObject = (ObjectNode) parse_result;
        
        System.out.println("parse_result : " + parse_result);
        System.out.println("parse_result type : " + parse_result.getClass().getName());
        System.out.println("ObjectNode resultObject : " + resultObject.get("pareto"));
        
        // 기존 Result image_to_base64 parsing
        // ArrayNode parse_pareto = (ArrayNode) parse_result.get( "pareto");
        // ArrayNode parse_contour = (ArrayNode) parse_result.get("contour");
        // ArrayNode parse_response = (ArrayNode) parse_result.get("response");
        
        JsonNode parse_pareto = null;
        JsonNode parse_contour = null;
        JsonNode parse_response = null;
        
        // **********---pareto---**********
        if (parse_result.has("pareto") && parse_result.get("pareto").isArray()) {
            parse_pareto = parse_result.get("pareto");
            System.out.println("******pareto******");
            System.out.println(parse_pareto);
        } else {
            System.out.println("'pareto' field is missing or not an array.");
        }
        
        // **********---contour---**********
        if (parse_result.has("contour") && parse_result.get("contour").isArray()) {
            parse_contour = parse_result.get("contour");
            System.out.println("******contour******");
            System.out.println(parse_contour);
        } else {
            System.out.println("'contour' field is missing or not an array.");
        }
        
        // **********---response---**********
        if (parse_result.has("response") && parse_result.get("response").isArray()) {
            parse_response = parse_result.get("response");
            System.out.println("******response******");
            System.out.println(parse_response);
        } else {
            System.out.println("'response' field is missing or not an array.");
        }
        
        // ** start binary 데이터가 빈값으로 들어오는 경우 예외처리*/
        if (StringUtils.isEmpty(String.valueOf(parse_pareto)) || StringUtils.isEmpty(String.valueOf(parse_contour))
                || StringUtils.isEmpty(String.valueOf(parse_response))) {
            // 추가 (code, msg) jsp에서 code가 000이 아닌 경우 해당 msg를 alert창으로 보여주게끔 되어있음
            ((ObjectNode)data).put("code", "111");
            ((ObjectNode)data).put("msg", "이미지 binary 데이터 값이 비어있습니다. 다시 확인해주세요.");
            resultStats.put("resultData", data);
            return resultJSON; 
        }
        
        /** start binary 데이터 이미지 저장*/
        // base64 -> 이미지 저장 후 경로를 담을 list 변수 선언
        //ArrayList<String> pareto_list = getGraphList(path1, parse_pareto);
        //ArrayList<String> contour_list = getGraphList(path2, parse_contour);
        //ArrayList<String> response_list = getGraphList(path3, parse_response);
        
        // 기존 path로 주어지던 key값 활용
        //((ObjectNode)parse_result).put("pareto", om.valueToTree(pareto_list));
        //((ObjectNode)parse_result).put("contour", om.valueToTree(contour_list));
        //((ObjectNode)parse_result).put("response", om.valueToTree(response_list));
        /** end binary 데이터 이미지 저장 */
        
        System.out.println("최종 response check : " + data);
    /// ********************************************
    /// *************** TRY_API 요청 끝 ***************
    /// ********************************************
    } catch (MalformedURLException e) {
        log.error("######### 예외 발생65 ##########");
    } catch (IOException e) {
        log.error("######### 예외 발생66 ##########");
    } catch (JSONException e) {
        log.error("######### 예외 발생67 ##########");
    } finally {
        in.close();
    }
    
    JSONParser parser = new JSONParser();
    org.json.simple.JSONObject jsonObj = (org.json.simple.JSONObject) parser.parse(data.toString());

    // 원본
    org.json.simple.JSONObject jsons = (org.json.simple.JSONObject) jsonObj.get("result");
    org.json.simple.JSONArray responseList = (org.json.simple.JSONArray) jsons.get("response");
    org.json.simple.JSONArray contourList = (org.json.simple.JSONArray) jsons.get("contour");
    org.json.simple.JSONArray paretoList = (org.json.simple.JSONArray) jsons.get("pareto");
    
    DataMap outputData = new DataMap();
    // *******************************************
    // ***************-image_write-***************
    // *******************************************
    
        // ************** Pareto_Plot **************
    System.out.println("*** Pareto_Plot ***");
    for (int i=0; i< paretoList.size(); i++) {
        org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) paretoList.get(i);
        
        System.out.println("ParetoList"+ i + "번째 image_name :" + (String) jsonObject.get("image_name"));
        
        String parse_text = (String) jsonObject.get("image_to_base64");
        
        byte [] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(parse_text);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
        ImageIO.write(img, "png", new File(pareto_path + (String) jsonObject.get("image_name") + ".png"));
        
        outputData.put("preto_path_" + (i+1), pareto_path + (String) jsonObject.get("image_name") + ".png");
    }
    
        // ************** Contour_Plot **************
    System.out.println("*** Contour_Plot ***");
    for (int i=0; i< contourList.size(); i++) {
        org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) contourList.get(i);
        
        System.out.println("ContourList"+ i + "번째 image_name :" + (String) jsonObject.get("image_name"));
        
        String parse_text = (String) jsonObject.get("image_to_base64");
        
        byte [] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(parse_text);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
        ImageIO.write(img, "png", new File(contour_path + (String) jsonObject.get("image_name") + ".png"));
        
        outputData.put("contour_path_" + (i+1), contour_path + (String) jsonObject.get("image_name") + ".png");
    }
    
        // ************** Response_Plot **************
    System.out.println("*** Response_Plot ***");
    for (int i=0; i< responseList.size(); i++) {
        org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) responseList.get(i);
        
        System.out.println("ResponseList"+ i + "번째 image_name :" + (String) jsonObject.get("image_name"));
        
        String parse_text = (String) jsonObject.get("image_to_base64");
        
        byte [] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(parse_text);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
        ImageIO.write(img, "png", new File(response_path + (String) jsonObject.get("image_name") + ".png"));
        
        outputData.put("response_path_" + (i+1), response_path + (String) jsonObject.get("image_name") + ".png");
    }
    
    System.out.println("************_PRINT-END_************");
    
    // ***********************************************
    // ***************-Image_Write_End-***************
    // ***********************************************
    
    UserInfoVo userInfoVo = SessionUtil.getSessionUserInfoVo(request);
    outputData.put("prjct_id", param.getString("prjct_id"));
    outputData.put("status", param.getString("status"));
    outputData.put("ss_user_no", userInfoVo.getUserNo());
    
    System.out.println("ouputData : " + outputData);
    // insert 및 insert 하기전 update(USE_YN = 'N')
    formulationService.stepChangeFunc(outputData);

    doeService.insertStep6Graph(outputData);

    resultStats.put("resultData", data);
    resultJSON.put("resultStats", resultStats);
    
    System.out.println("resultData : " + data);
    System.out.println("ResultStats : " + resultStats);
    
    return resultJSON;
}