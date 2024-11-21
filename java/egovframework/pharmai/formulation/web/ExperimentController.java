package egovframework.pharmai.formulation.web;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import egovframework.admin.common.vo.UserInfoVo;
import egovframework.framework.common.object.DataMap;
import egovframework.framework.common.util.EgovMessageSource;
import egovframework.framework.common.util.EgovPropertiesUtil;
import egovframework.framework.common.util.RequestUtil;
import egovframework.framework.common.util.SessionUtil;
import egovframework.framework.common.util.file.NtsysFileMngUtil;
import egovframework.framework.common.util.file.service.NtsysFileMngService;
import egovframework.pharmai.formulation.service.ExperimentService;
import egovframework.pharmai.formulation.service.FormulationService;
import egovframework.pharmai.formulation.service.CqasService;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 *
 * <PRE>
 * 1. ClassName :
 * 2. FileName  : formulationController.java
 * 3. Package  : egovframework.pharmAi.formulation.web
 * 4. Comment  :
 * 5. 작성자   : :LWJ
 * 6. 작성일   : 2021. 5. 21. 오후 1:17:50
 * </PRE>
 */
@Controller
public class ExperimentController {

	private static Log log = LogFactory.getLog(ExperimentController.class);

	@Resource(name = "egovMessageSource")
	private EgovMessageSource egovMessageSource;

	@Resource(name = "formulationService")
	private FormulationService formulationService;

	@Resource(name = "cqasService")
	private CqasService cqasService;
	
	@Resource(name = "experimentService")
	private ExperimentService experimentService;
	
	@Resource(name = "NtsysFileMngUtil")
	private NtsysFileMngUtil ntsysFileMngUtil;

	@Resource(name = "NtsysFileMngService")
	private NtsysFileMngService ntsysFileMngService;

	/*STEP5 시작*/
	@RequestMapping(value = "/pharmai/chemical/formulation/selectExperiment.do")
	public String selectExperiment(HttpServletRequest request, HttpServletResponse response, ModelMap model)
			throws Exception {
		
		System.out.println("#########ExperimentController_selectExperiment#########");
		DataMap param = RequestUtil.getDataMap(request);
		
		UserInfoVo userInfoVo = SessionUtil.getSessionUserInfoVo(request);
		System.out.println("SelectExperiment_USERINFO :" + userInfoVo);
		
		param.put("prjct_id", param.getString("prjct_id", userInfoVo.getCur_prjct_id()));
		param.put("prjct_type", param.getString("prjct_type", userInfoVo.getCur_prjct_type()));
		param.put("status", param.getString("status", "05"));
		param.put("userNo", userInfoVo.getUserNo());
		
		String next_data = formulationService.selectNextDataExt(param);
		param.put("next_data", next_data);

		List exprDataList = experimentService.selectListFormulaStp_05(param);
		// step3에서 체크된 부형제 갯수 (구역 구분을 위해서)
		int selectStp3TotYn = experimentService.selectStp3TotYn(param);
		int stpColumncount = experimentService.selectStp5ColumnCount();

		if (exprDataList != null && exprDataList.size() > 0) { //db통신
			model.addAttribute("exprDataList", exprDataList);
			param.put("selectStp3TotYn",selectStp3TotYn);
			param.put("stpColumncount",stpColumncount);
			param.put("step_new", "N");
		} else {
			param.put("step_new", "Y");
		}
		DataMap pjtData = formulationService.selectPjtMst(param);//project master
		model.addAttribute("pjtData", pjtData);
		model.addAttribute("param", param);
		System.out.println("select Experiment_Param : " + param);
		
		/* 추가 코드
		/* ################################### */
		/* #### DOE 추출에 따른 다른 JSP 불러오기 #### */
		/* ################################### */
		List existDoE = cqasService.selectListFormulaPrj_DoE(param);
		DataMap dataMap = (DataMap) existDoE.get(0);
		String DoEType = (String) dataMap.get("DoE_Type");
		String return_url = "";
		
		if (DoEType.equals("SLD")){
			return_url = "pharmai/formulation/selectExperiment_SLD";
		}
		else {
			return_url = "pharmai/formulation/selectExperiment";
		}

		return return_url;
	}


	@RequestMapping(value = "/pharmai/formulation/getApi5Ajax.do")
	public @ResponseBody DataMap getApi5Ajax(HttpServletRequest request, HttpServletResponse response, ModelMap model)
			throws Exception {

		log.debug("####" + this.getClass().getName() + " START ####");
		DataMap param = RequestUtil.getDataMap(request);

		URL url;
		HttpURLConnection conn;

		StringBuffer sb = new StringBuffer();
		BufferedReader in = null;

		JSONObject jsonob = new JSONObject();

		JSONArray jsonarr = new JSONArray();
		JSONArray jsonarrFormulation = new JSONArray();
		JSONArray jsonArrPrimary = new JSONArray();
		Object data = new Object();

		DataMap cqasMap = null;
		
		System.out.println("selectStp_03 param : " + param);
		List selectStp_03 = experimentService.selectListFormulaStp_03(param);

		DataMap selectStp_03Map = null;
		JSONObject inputData = new JSONObject();

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

		inputData.put("formulation", jsonarrFormulation);

		List cqasList = experimentService.selectListFormulaStp_04(param);

		JSONArray cqadata = new JSONArray();
		for (Object list : cqasList) {
			cqasMap = (DataMap) list;
			if (cqasMap.getString("CHECK_YN").equals("Y")) {
				cqadata.add(cqasMap.getString("CQA_NM"));
			}
		}
		inputData.put("CQAs", cqadata);

		jsonarr.add(inputData);
		jsonArrPrimary.add(jsonarr);
		jsonob.put("data", jsonArrPrimary);
		
		System.out.println("CQA DATA : " + cqadata); //["appearance", "assay"]
		System.out.println("Experiment jsonob : " + jsonob.toString());
		
		//#################################################################
		//##################수정 중 DoE 방식에 따른 API URL 수정##################
		//#################################################################
		// 수정 필요
		List formulation_DoE = cqasService.selectListFormulaPrj_DoE(param);
		DataMap doe_data = (DataMap) formulation_DoE.get(0);  // 첫 번째 DataMap 객체 가져오기
	    String DoE = doe_data.getString("DoE_Type");
		
	    System.out.println("REQUEST_DoE : " + DoE);
		System.out.println(DoE);
	    
		String formulation_api5 = "";
	    
		// DoE에 따른 api5 url 설정
		if (DoE.equals("CCD")) {
			System.out.println("if CCD");
			formulation_api5 = "Globals.api.formulation.api5_CCD";
			URL API_5_url = new URL(EgovPropertiesUtil.getProperty(formulation_api5));
			System.out.println("API_5_url : " + API_5_url);
		}
		else if (DoE.equals("BBD")) {
			System.out.println("if BBD");
			formulation_api5 = "Globals.api.formulation.api5_BBD";
			URL API_5_url = new URL(EgovPropertiesUtil.getProperty(formulation_api5));
			System.out.println("API_5_url : " + API_5_url);
		}
		else if (DoE.equals("SLD")) {
			System.out.println("if SLD");
			formulation_api5 = "Globals.api.formulation.api5_SLD";
			URL API_5_url = new URL(EgovPropertiesUtil.getProperty(formulation_api5));
			System.out.println("API_5_url : " + API_5_url);
		}
		
		System.out.println("api 5 URL : " + formulation_api5);
		//#################################################################
		//#################################################################
		//#################################################################
		
		System.out.println("");
		
		try {
			url = new URL(EgovPropertiesUtil.getProperty(formulation_api5));
			System.out.println("request_url : " + url);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setRequestProperty("Accept", "application/json");
			conn.setConnectTimeout(99999);
			conn.setReadTimeout(99999);
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

			conn.disconnect();

			log.debug(sb.toString());
			JSONParser parser = new JSONParser();
			data = parser.parse(sb.toString());

		} catch (MalformedURLException e) {
			System.out.println("error : " + e);
			log.error("######### 예외 발생65 ##########");
		} catch (IOException e) {
			System.out.println("error : " + e);
			log.error("######### 예외 발생66 ##########");
		} catch (JSONException e) {
			System.out.println("error : " + e);
			log.error("######### 예외 발생67 ##########");
		} finally {
			in.close();
		}

		DataMap resultJSON = new DataMap();

		// return 상태
		DataMap resultStats = new DataMap();
		resultStats.put("resultData", data);
		resultJSON.put("resultStats", resultStats);
		
		System.out.println("api 5 response : " + resultJSON);
		
		return resultJSON;
	}

	// STEP5 NEXT시 
	@RequestMapping(value = "/pharmai/formulation/saveStep5.do")
	public String saveStep5(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		DataMap param = RequestUtil.getDataMap(request);
		
		System.out.println("#########ExperimentController_saveStep5#########");
		
		UserInfoVo userInfoVo = SessionUtil.getSessionUserInfoVo(request);
		param.put("ss_user_no", userInfoVo.getUserNo());

		formulationService.stepChangeFunc(param);
		System.out.println("saveStep5_request : " + request); // org.apache.catalina
		System.out.println("saveStep5 : " + param);
		
		// insert
		experimentService.insertFormulaStp_05(param);
		
		//마지막 수정일
		formulationService.updatePjt_mst(param);

		return "redirect: /pharmai/chemical/formulation/selectDoE.do";
	}


	@RequestMapping(value = "/pharmai/formulation/saveFormulTemporary.do")
	public String saveFormulTemporary(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception{
		System.out.println("#########ExperimentController_Temporary#########");
		DataMap param = RequestUtil.getDataMap(request);

		UserInfoVo userInfoVo = SessionUtil.getSessionUserInfoVo(request);
		param.put("ss_user_no", userInfoVo.getUserNo());

		formulationService.stepChangeFunc(param);

		experimentService.formulTemporarySave(param);

		return "redirect: /pharmai/chemical/formulation/selectExperiment.do?prjct_id =" + param.getString("prjct_id");
	}

	/* STEP5 종료 */

}
