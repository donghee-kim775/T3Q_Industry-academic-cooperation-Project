package egovframework.pharmai.formulation.web;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.JSONParser;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import egovframework.admin.common.vo.UserInfoVo;
import egovframework.bio.formulation.service.BioExperimentService;
import egovframework.framework.common.dao.CommonMybatisDao;
import egovframework.framework.common.constant.Globals;
import egovframework.framework.common.object.DataMap;
import egovframework.framework.common.util.EgovMessageSource;
import egovframework.framework.common.util.EgovPropertiesUtil;
import egovframework.framework.common.util.RequestUtil;
import egovframework.framework.common.util.SessionUtil;
import egovframework.framework.common.util.file.NtsysFileMngUtil;
import egovframework.framework.common.util.file.service.NtsysFileMngService;
import egovframework.pharmai.formulation.service.CqasService;
import egovframework.pharmai.formulation.service.DoeService;
import egovframework.pharmai.formulation.service.ExperimentService;
import egovframework.pharmai.formulation.service.FormulationService;
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
public class DoeController {

	private static Log log = LogFactory.getLog(DoeController.class);

	@Resource(name="commonMybatisDao")
	private CommonMybatisDao commonMybatisDao;
	
	@Resource(name = "bioExperimentService")
	private BioExperimentService bioExperimentService;
	
	@Resource(name = "egovMessageSource")
	private EgovMessageSource egovMessageSource;

	@Resource(name = "formulationService")
	private FormulationService formulationService;

	@Resource(name = "experimentService")
	private ExperimentService experimentService;

	@Resource(name = "doeService")
	private DoeService doeService;

	@Resource(name = "cqasService")
	private CqasService cqasService;
	
	@Resource(name = "NtsysFileMngUtil")
	private NtsysFileMngUtil ntsysFileMngUtil;

	@Resource(name = "NtsysFileMngService")
	private NtsysFileMngService ntsysFileMngService;
	
	public void deleteDirectoryContents(File directory) {
	    if (directory.exists()) {
	        File[] files = directory.listFiles();
	        if (files != null) {
	            for (File file : files) {
	                if (file.isDirectory()) {
	                    deleteDirectoryContents(file);
	                }
	                file.delete();
	            }
	        }
	    }
	}
	
	/* STEP6 시작 */
	@RequestMapping(value = "/pharmai/chemical/formulation/selectDoE.do")
	public String formulationResult(HttpServletRequest request, HttpServletResponse response, ModelMap model)
			throws Exception {
		DataMap param = RequestUtil.getDataMap(request);

		System.out.println("#############formulationResult Controller#############");

		String common_path = EgovPropertiesUtil.getProperty("Globals.fileWebrootPath.formulation");

		UserInfoVo userInfoVo = SessionUtil.getSessionUserInfoVo(request);
		param.put("prjct_id", param.getString("prjct_id", userInfoVo.getCur_prjct_id()));
		param.put("prjct_type", param.getString("prjct_type", userInfoVo.getCur_prjct_type()));
		param.put("status", param.getString("status", "06"));
		param.put("userNo", userInfoVo.getUserNo());

		DataMap selectStep6Graph_preto = doeService.selectStep6Graph_preto(param);
		DataMap selectStep6Graph_contour = doeService.selectStep6Graph_contour(param);
		DataMap selectStep6Graph_response = doeService.selectStep6Graph_response(param);

		if (selectStep6Graph_preto == null) {
			param.put("step_new", "Y");
		} else { // db통신
			param.put("step_new", "N");
			DataMap preto_file_nm = new DataMap();
			DataMap contour_file_nm = new DataMap();
			DataMap response_file_nm = new DataMap();
			
			for (int i = 0; i < 5; i++) {
				String tempData = selectStep6Graph_preto.getString("PRETO_PATH_" + (i + 1))
						.replace("/home/data/t3q/uploads/", "/upload/");
				selectStep6Graph_preto.put("PRETO_PATH_" + (i + 1), tempData);
				String[] str = tempData.split("/");
				String fileName = str[str.length - 1];
				if (tempData != "") {
					int dot = fileName.lastIndexOf('.');
					String _fileName = (dot>-1)?fileName.substring(0,dot):fileName;
					preto_file_nm.put("PRETO_PATH_" + (i + 1) + "_NM", _fileName);
				} else {
					preto_file_nm.put("PRETO_PATH_" + (i + 1) + "_NM", fileName);
				}
			}

			model.addAttribute("selectStep6Graph_preto", selectStep6Graph_preto);
			model.addAttribute("preto_file_nm", preto_file_nm);
			
			for (int i = 0; i < 15; i++) {
				String tempData = selectStep6Graph_contour.getString("CONTOUR_PATH_" + (i + 1))
						.replace("/home/data/t3q/uploads/", "/upload/");
//				selectStep6Graph_contour.put("CONTOUR_PATH_" + i, tempData);
				selectStep6Graph_contour.put("CONTOUR_PATH_" + (i + 1), tempData);
				String[] str = tempData.split("/");
				String fileName = str[str.length - 1];
				if (tempData != "") {
					int dot = fileName.lastIndexOf('.');
					String _fileName = (dot>-1)?fileName.substring(0,dot):fileName;
					contour_file_nm.put("CONTOUR_PATH_" + (i + 1) + "_NM", _fileName);
				} else {
					contour_file_nm.put("CONTOUR_PATH_" + (i + 1) + "_NM", fileName);
				}
			}

			model.addAttribute("selectStep6Graph_contour", selectStep6Graph_contour);
			model.addAttribute("contour_file_nm", contour_file_nm);
			
			for (int i = 0; i < 15; i++) {
				String tempData = selectStep6Graph_response.getString("RESPONSE_PATH_" + (i + 1))
						.replace("/home/data/t3q/uploads/", "/upload/");
				;
//				selectStep6Graph_response.put("RESPONSE_PATH_" + i, tempData);
				selectStep6Graph_response.put("RESPONSE_PATH_" + (i + 1), tempData);
				String[] str = tempData.split("/");
				String fileName = str[str.length - 1];
				if (tempData != "") {
					int dot = fileName.lastIndexOf('.');
					String _fileName = (dot>-1)?fileName.substring(0,dot):fileName;
					response_file_nm.put("RESPONSE_PATH_" + (i + 1) + "_NM", _fileName);
				} else {
					response_file_nm.put("RESPONSE_PATH_" + (i + 1) + "_NM", fileName);
				}
			}

			model.addAttribute("selectStep6Graph_response", selectStep6Graph_response);
			model.addAttribute("response_file_nm", response_file_nm);

		}

		List selectStep6Design = doeService.selectStep6Design(param);
		DataMap selectStep6Design_img = doeService.selectStep6Design_img(param);
		DataMap design_file_nm = new DataMap();

		// api7_design 값 체크
		if (selectStep6Design.isEmpty() && selectStep6Design_img == null) {
			param.put("step_new", "Y");
		} else { // db통신
			int Design_length = selectStep6Design.size();

			for (int i = 0; i < 3; i++) {
				String tempData = selectStep6Design_img.getString("DESIGN_IMG_PATH_" + (i + 1))
						.replace("/home/data/t3q/uploads/", "/upload/");
				selectStep6Design_img.put("DESIGN_IMG_PATH_" + (i + 1), tempData);
				String[] str = tempData.split("/");
				String fileName = str[str.length - 1];
				if (tempData != "") {
					int dot = fileName.lastIndexOf('.');
					String _fileName = (dot>-1)?fileName.substring(0,dot):fileName;
					design_file_nm.put("DESIGN_IMG_PATH_" + (i + 1) + "_NM", _fileName);
				} else {
					design_file_nm.put("DESIGN_IMG_PATH_" + (i + 1) + "_NM", fileName);
				}
			}

			model.addAttribute("Design_length", Design_length);
			model.addAttribute("selectStep6Design", selectStep6Design);
			model.addAttribute("selectStep6Design_img", selectStep6Design_img);
			model.addAttribute("design_file_nm", design_file_nm);
		}
		
		List selectStep6ResultList = doeService.selectStp07result(param);
		DataMap selectStepResultImgData = doeService.selectStp07resultImg(param);
		DataMap result_file_nm = new DataMap();
		// api호출
		if (selectStep6ResultList.size() == 0 || selectStepResultImgData == null) {
			param.put("result_step_status", "Y");
		} else { // db호출
			param.put("result_step_status", "N");
			for (int i = 0; i < 18; i++) {
				String tempData = selectStepResultImgData.getString("CONTOUR_IMG_PATH_" + (i + 1))
						.replace("/home/data/t3q/uploads/", "/upload/");
				;
				selectStepResultImgData.put("CONTOUR_IMG_PATH_" + (i + 1), tempData);
				String[] str = tempData.split("/");
				String fileName = str[str.length - 1];
				if (tempData != "") {
					int dot = fileName.lastIndexOf('.');
					String _fileName = (dot>-1)?fileName.substring(0,dot):fileName;
					result_file_nm.put("CONTOUR_IMG_PATH_" + (i + 1) + "_NM", _fileName);
				} else {
					result_file_nm.put("CONTOUR_IMG_PATH_" + (i + 1) + "_NM", fileName);
				}
			}

			for (int i = 0; i < 3; i++) {
				String tempData = selectStepResultImgData.getString("DESIGN_IMG_PATH_" + (i + 1))
						.replace("/home/data/t3q/uploads/", "/upload/");
				;
				selectStepResultImgData.put("DESIGN_IMG_PATH_" + (i + 1), tempData);
				String[] str = tempData.split("/");
				String fileName = str[str.length - 1];
				if (tempData != "") {
					int dot = fileName.lastIndexOf('.');
					String _fileName = (dot>-1)?fileName.substring(0,dot):fileName;
					result_file_nm.put("DESIGN_IMG_PATH_" + (i + 1) + "_NM", _fileName);
				} else {
					result_file_nm.put("DESIGN_IMG_PATH_" + (i + 1) + "_NM", fileName);
				}
			}

			List selectStp_06 = doeService.selectListFormulaStp_06(param);
			List selectStp_03 = experimentService.selectListFormulaStp_03(param);
			
			model.addAttribute("result_file_nm", result_file_nm);
			model.addAttribute("selectStep6ResultList", selectStep6ResultList);
			model.addAttribute("selectStepResultImgData", selectStepResultImgData);
			model.addAttribute("selectStp_06", selectStp_06);
			model.addAttribute("selectStp_03", selectStp_03);

		}

		DataMap pjtData = formulationService.selectPjtMst(param);// project master
		model.addAttribute("pjtData", pjtData);
		model.addAttribute("param", param);
		model.addAttribute("common_path", common_path);

		return "pharmai/formulation/selectDoE";
	}

	/**
	 * 
	 * @param path
	 * @param arrayNode
	 * @return arrayList
	 * @throws IOException
	 */
//	arryNode에 있는 이미지 개수에 따라서 이미지 생성 및 해당 이미지 경로와 이미지 명 arrayList로 return
	private ArrayList<String> getGraphList(String path, ArrayNode arrayNode) throws IOException {
		ArrayList returnList = new ArrayList<>();

		for (int i = 0; i < arrayNode.size(); i++) {
			JsonNode jsonObject = arrayNode.get(i);
			// string을 byte 형식으로 변환 후 이미지 저장 (string ->
			// javax.xml.bind.DatatypeConverter.parseBase64Binary)
			byte[] imageBytes = javax.xml.bind.DatatypeConverter
					.parseBase64Binary(jsonObject.get("image_to_base64").asText());
			// 특정 경로에 이미지 저장 (param > path)
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
			String fileName = path + jsonObject.get("image_name").asText() + ".png";
			System.out.println("fileName check : " + fileName);
			ImageIO.write(img, "png", new File(fileName));
			// 해당 이미지를 list에 담기
			returnList.add(fileName);
		}

		return returnList;
	}

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

		deleteDirectoryContents(directory1);
		deleteDirectoryContents(directory2);
		deleteDirectoryContents(directory3);

		if (!directory1.exists()) {
			System.out.println("directory 1 존재 x");
		    directory1.mkdirs();
		}
		if (!directory2.exists()) {
			System.out.println("directory 2 존재 x");
		    directory2.mkdirs();
		}
		if (!directory3.exists()) {
			System.out.println("directory 3 존재 x");
		    directory3.mkdirs();
		}

		/// *********************************************
		/// ***************Complete-Request**************
		/// *********************************************
		
		List formulation_DoE = cqasService.selectListFormulaPrj_DoE(param);
		DataMap doe_data = (DataMap) formulation_DoE.get(0);  // 첫 번째 DataMap 객체 가져오기
	    String DoE = doe_data.getString("DoE_Type");
		
	    System.out.println("REQUEST_DoE : " + DoE);
		System.out.println(DoE);
	    
		String formulation_api6 = "";

		// DoE에 따른 api5 url 설정
		if (DoE.equals("CCD")) {
			System.out.println("if CCD");
			formulation_api6 = "Globals.api.formulation.api6_CCD";
			URL API_6_url = new URL(EgovPropertiesUtil.getProperty(formulation_api6));
			System.out.println("API_6_url : " + API_6_url);
		}
		else if (DoE.equals("BBD")) {
			System.out.println("if BBD");
			formulation_api6 = "Globals.api.formulation.api6_BBD";
			URL API_6_url = new URL(EgovPropertiesUtil.getProperty(formulation_api6));
			System.out.println("API_6_url : " + API_6_url);
		}
		else if (DoE.equals("SLD")) {
			System.out.println("if SLD");
			formulation_api6 = "Globals.api.formulation.api6_SLD";
			URL API_6_url = new URL(EgovPropertiesUtil.getProperty(formulation_api6));
			System.out.println("API_6_url : " + API_6_url);
		}
		
		System.out.println("api 6 URL : " + formulation_api6);		
		
		jsonarr.add(jdata);
		jsonArrPrimary.add(jsonarr);
		jsonob.put("data", jsonArrPrimary);
		System.out.println("최종 data check : " + jsonob);
		
		
		/// *********************************************
		/// **************API6_Refac-Request*************
		/// *********************************************		
		try {
			url = new URL(EgovPropertiesUtil.getProperty(formulation_api6));
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setRequestProperty("Accept", "application/json");
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
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
			System.out.println("response_data : " + data);
			
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
			
			JsonNode parse_anova = null;
			JsonNode parse_pareto = null;
			JsonNode parse_contour = null;
			JsonNode parse_response = null;
			
			// **********---anova---**********
			if (parse_result.has("anova") && parse_result.get("anova").isArray()) {
	            parse_anova = parse_result.get("anova");
	            System.out.println("******anova******");
	            System.out.println(parse_anova);
	        } else {
	            System.out.println("'pareto' field is missing or not an array.");
	        }
			
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
		org.json.simple.JSONArray anovaList = (org.json.simple.JSONArray) jsons.get("anova");
		org.json.simple.JSONArray responseList = (org.json.simple.JSONArray) jsons.get("response");
		org.json.simple.JSONArray contourList = (org.json.simple.JSONArray) jsons.get("contour");
		org.json.simple.JSONArray paretoList = (org.json.simple.JSONArray) jsons.get("pareto");
		
		System.out.println("anovaList : " + anovaList);
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		ObjectNode imagePathNode = objectMapper.createObjectNode();
		
		ArrayNode contourPathArray = objectMapper.createArrayNode();
		ArrayNode responsePathArray = objectMapper.createArrayNode();
		ArrayNode paretoPathArray = objectMapper.createArrayNode();
		
		DataMap outputData = new DataMap();
		// *******************************************
		// ***************-image_write-***************
		// *******************************************
		
			// ************** Pareto_Plot **************
		System.out.println("*** Pareto_Plot ***");
		for (int i=0; i< paretoList.size() + 1; i++) {
			if (i == paretoList.size()) {
				imagePathNode.set("pareto", paretoPathArray);
			}
			else {
				org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) paretoList.get(i);
				
				System.out.println("ParetoList"+ i + "번째 image_name :" + (String) jsonObject.get("image_name"));
				
				String parse_text = (String) jsonObject.get("image_to_base64");
				
				byte [] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(parse_text);
				BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
				ImageIO.write(img, "png", new File(pareto_path + (String) jsonObject.get("image_name") + ".png"));
				
				outputData.put("preto_path_" + (i+1), pareto_path + (String) jsonObject.get("image_name") + ".png");
				paretoPathArray.add(pareto_path + (String) jsonObject.get("image_name") + ".png");
			}
		}
		
			// ************** Contour_Plot **************
		System.out.println("*** Contour_Plot ***");
		for (int i=0; i< contourList.size() + 1; i++) {
			if (i == contourList.size()) {
				imagePathNode.set("contour", contourPathArray);
			}
			else {
				org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) contourList.get(i);
				
				System.out.println("ContourList"+ i + "번째 image_name :" + (String) jsonObject.get("image_name"));
				
				String parse_text = (String) jsonObject.get("image_to_base64");
				
				byte [] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(parse_text);
				BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
				ImageIO.write(img, "png", new File(contour_path + (String) jsonObject.get("image_name") + ".png"));
				
				outputData.put("contour_path_" + (i+1), contour_path + (String) jsonObject.get("image_name") + ".png");
				contourPathArray.add(pareto_path + (String) jsonObject.get("image_name") + ".png");
			}
		}
		
			// ************** Response_Plot **************
		System.out.println("*** Response_Plot ***");
		for (int i=0; i< responseList.size() + 1; i++) {
			if (i == responseList.size()){
				imagePathNode.set("response", responsePathArray);
			}
			else {
				org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) responseList.get(i);
				
				System.out.println("ResponseList"+ i + "번째 image_name :" + (String) jsonObject.get("image_name"));
				
				String parse_text = (String) jsonObject.get("image_to_base64");
				
				byte [] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(parse_text);
				BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
				ImageIO.write(img, "png", new File(response_path + (String) jsonObject.get("image_name") + ".png"));
				
				outputData.put("response_path_" + (i+1), response_path + (String) jsonObject.get("image_name") + ".png");
				responsePathArray.add(pareto_path + (String) jsonObject.get("image_name") + ".png");
			}
		}
		
		System.out.println("************_PRINT-END_************");
		
		// ***********************************************
		// ***************-Image_Write_End-***************
		// ***********************************************
		
		UserInfoVo userInfoVo = SessionUtil.getSessionUserInfoVo(request);
		outputData.put("prjct_id", param.getString("prjct_id"));
		outputData.put("status", param.getString("status"));
		outputData.put("ss_user_no", userInfoVo.getUserNo());
		

		System.out.println("outputData : " + outputData);
		System.out.println("outputData TYPE : " + outputData.getClass().getName());
		// insert 및 insert 하기전 update(USE_YN = 'N')
		formulationService.stepChangeFunc(outputData);

		doeService.insertStep6Graph(outputData);
		((ObjectNode)data).set("image_path", imagePathNode);
		
		resultStats.put("resultData", data);
		resultJSON.put("resultStats", resultStats);
		
		System.out.println("resultData : " + data);
		System.out.println("resultData TYPE : " + data.getClass().getName());
		System.out.println("ResultStats : " + resultStats);
		System.out.println("ResultStats TYPE : " + resultStats.getClass().getName());
		System.out.println("resultJSON: " + resultJSON);
		return resultJSON;
	}

	@RequestMapping(value = "/pharmai/formulation/getApi7Ajax.do")
	public @ResponseBody DataMap getApi6_resultAjax(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws Exception {
		// return 상태
		DataMap resultStats = new DataMap();
		
		log.debug("####" + this.getClass().getName() + " START ####");
		DataMap resultJSON = new DataMap();
		UserInfoVo userInfoVo = SessionUtil.getSessionUserInfoVo(request);
		DataMap param = RequestUtil.getDataMap(request);

		URL url;
		HttpURLConnection conn;

		StringBuffer sb = new StringBuffer();
		BufferedReader in = null;

		JSONObject jsonob = new JSONObject();
		JSONObject jdata = new JSONObject();
		JSONArray jsonarr = new JSONArray();
		JSONArray jsonarrFormulation = new JSONArray();
		JSONArray jsonArrPrimary = new JSONArray();

		// API 호출 후 결과를 json 으로 파싱 후 매핑
		JsonNode data = null;

		DataMap selectStp_03Map = null;
		DataMap selectStp_04Map = null;
		JSONArray selectStp_04List = new JSONArray();

		List selectStp_03 = experimentService.selectListFormulaStp_03(param);
		List selectStp_04 = experimentService.selectListFormulaStp_04(param);

		for (Object list : selectStp_04) {
			selectStp_04Map = (DataMap) list;
			if (selectStp_04Map.getString("CHECK_YN").equals("Y")) {
				selectStp_04List.add(selectStp_04Map.getString("CQA_NM"));

			}
		}
		jdata.put("header", selectStp_04List);
		jdata.put("experiment data", param.getString("experiment_data"));

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

		jdata.put("design-path", param.getString("design-path"));
		jdata.put("result-path", param.getString("result-path"));

//		 add 22.05.30 (UI에서 이미지 저장 시 경로 지정 및 생성)
		String prjct_id = param.getString("prjct_id");
		String path1 = EgovPropertiesUtil.getProperty("Globals.fileWebrootPath.formulation") + prjct_id
				+ "/api7/tab-1/design/";
		String path2 = EgovPropertiesUtil.getProperty("Globals.fileWebrootPath.formulation") + prjct_id
				+ "/api7/tab-2/result/";

		// path에 해당하는 디렉토리가 존재하지 않는 경우 해당 디렉토리 생성
		File directory1 = new File(path1);
		File directory2 = new File(path2);

		if (!directory1.exists() || !directory2.exists()) {
			directory1.mkdirs();
			directory2.mkdirs();
		}

		String[] y_variable_list = request.getParameterValues("y_variables[]");
		String[] effects_list = request.getParameterValues("effects[]");
		String[] ipt_start_ys_list = request.getParameterValues("ipt_start_ys[]");
		String[] ipt_end_ys_list = request.getParameterValues("ipt_end_ys[]");

		JSONArray jsonarrFormulation2 = new JSONArray();
		JSONObject jsonob2 = new JSONObject();

		for (int i = 0; i < y_variable_list.length; i++) {
			JSONObject temp = new JSONObject();
			JSONObject temp2 = new JSONObject();
			JSONArray jsonarrFormulation3 = new JSONArray();

			temp2.put("min", ipt_start_ys_list[i]);
			temp2.put("max", ipt_end_ys_list[i]);
			temp.put(effects_list[i], temp2);

			jsonarrFormulation3.add(temp);
			jsonob2.put(y_variable_list[i], jsonarrFormulation3);

		}
		jsonarrFormulation2.add(jsonob2);
		jdata.put("response", jsonarrFormulation2);

		jsonarr.add(jdata);
		jsonArrPrimary.add(jsonarr);
		jsonob.put("data", jsonArrPrimary);

		try {

			url = new URL(EgovPropertiesUtil.getProperty("Globals.api.formulation.api7"));// 주소 추가 필여
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
			conn.disconnect();

			ObjectMapper om = new ObjectMapper();
			data = om.readTree(sb.toString());
			
			// output data에 globals.properties env 값 전달
			((ObjectNode)data).put("system_env", Globals.SYSTEM_ENV);

			JsonNode parse_result = data.get("result");
			ArrayNode parse_design = (ArrayNode) parse_result.get("design");
			JsonNode parse_final = (JsonNode) parse_result.get("final");
			ArrayNode parse_contour = (ArrayNode) parse_final.get("contour");

			if (StringUtils.isEmpty(String.valueOf(parse_design)) || StringUtils.isEmpty(String.valueOf(parse_contour))) {
				// 추가 (code, msg) jsp에서 code가 000이 아닌 경우 해당 msg를 alert창으로 보여주게끔 되어있음
				((ObjectNode)data).put("code", "111");
				((ObjectNode)data).put("msg", "이미지 binary 데이터 값이 비어있습니다. 다시 확인해주세요.");
				resultStats.put("resultData", data);
				return resultJSON;
			}
			
			/** start binary 데이터 이미지 저장*/
			// base64 -> 이미지 저장 후 경로를 담을 list 변수 선언
			ArrayList<String> design_list = getGraphList(path1, parse_design);
			ArrayList<String> contour_list = getGraphList(path2, parse_contour);
			
			// 기존 path로 주어지던 key값 활용
			((ObjectNode)parse_result).put("design", om.valueToTree(design_list));
			((ObjectNode)parse_final).put("contour", om.valueToTree(contour_list));
			((ObjectNode)parse_final).put("design", om.valueToTree(design_list));
			
			/** end binary 데이터 이미지 저장 */

			System.out.println("최종 data check : " + data);

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
		
		org.json.simple.JSONObject jsonData = (org.json.simple.JSONObject) jsonObj.get("result");
		org.json.simple.JSONArray design = (org.json.simple.JSONArray) jsonData.get("design");

		for (int i = 0; i < design.size(); i++) {
			param.put("design_img_path_" + (i + 1), design.get(i));
		}

		org.json.simple.JSONArray excipientArr = (org.json.simple.JSONArray) jsonData.get("excipient");

		List<String> excipenetArrName = new ArrayList<>();
		List<String> excipenetArrMin = new ArrayList<>();
		List<String> excipenetArrMax = new ArrayList<>();
		List<String> controlMinArr = new ArrayList<>();
		List<String> controlMaxArr = new ArrayList<>();
		List<String> designMinArr = new ArrayList<>();
		List<String> designMaxArr = new ArrayList<>();
		List<String> knowledgeMinArr = new ArrayList<>();
		List<String> knowledgeMaxArr = new ArrayList<>();

		for (int i = 0; i < excipientArr.size(); i++) {
			org.json.simple.JSONObject obj = (org.json.simple.JSONObject) excipientArr.get(i);
			List list = new ArrayList<>(obj.keySet());
			excipenetArrName.add(list.get(0).toString());
			org.json.simple.JSONObject rangedata = (org.json.simple.JSONObject) obj.get(list.get(0).toString());
			String min = rangedata.get("min").toString();
			String max = rangedata.get("max").toString();
			excipenetArrMin.add(min);
			excipenetArrMax.add(max);

		}

		param.put("excipientNameArr", excipenetArrName);
		param.put("minArr", excipenetArrMin);
		param.put("maxArr", excipenetArrMax);

		org.json.simple.JSONArray factorArr = (org.json.simple.JSONArray) jsonData.get("factor");

		for (int i = 0; i < factorArr.size(); i++) {
			org.json.simple.JSONObject obj = (org.json.simple.JSONObject) factorArr.get(i);
			List list = new ArrayList<>(obj.keySet());
			org.json.simple.JSONObject factorObj = (org.json.simple.JSONObject) obj.get(list.get(0).toString());
			List factorNmList = new ArrayList<>(factorObj.keySet());
			org.json.simple.JSONObject SpaceObj = (org.json.simple.JSONObject) factorObj
					.get(factorNmList.get(0).toString());

			org.json.simple.JSONObject controlSpaceObj = (org.json.simple.JSONObject) SpaceObj.get("control space");
			String controlMin = controlSpaceObj.get("min").toString();
			String controlMax = controlSpaceObj.get("max").toString();

			controlMinArr.add(controlMin);
			controlMaxArr.add(controlMax);

			org.json.simple.JSONObject designSpaceObj = (org.json.simple.JSONObject) SpaceObj.get("design space");
			String designMin = designSpaceObj.get("min").toString();
			String designMax = designSpaceObj.get("max").toString();

			designMinArr.add(designMin);
			designMaxArr.add(designMax);

			org.json.simple.JSONObject knowledgeSpaceObj = (org.json.simple.JSONObject) SpaceObj.get("knowledge space");
			String knowledgeMin = knowledgeSpaceObj.get("min").toString();
			String knowledgeMax = knowledgeSpaceObj.get("max").toString();

			knowledgeMinArr.add(knowledgeMin);
			knowledgeMaxArr.add(knowledgeMax);
		}

		param.put("controlMinArr", controlMinArr);
		param.put("controlMaxArr", controlMaxArr);
		param.put("designMinArr", designMinArr);
		param.put("designMaxArr", designMaxArr);
		param.put("knowledgeMinArr", knowledgeMinArr);
		param.put("knowledgeMaxArr", knowledgeMaxArr);

		org.json.simple.JSONObject finalObj = (org.json.simple.JSONObject) jsonData.get("final");
		org.json.simple.JSONArray finalContourPng = (org.json.simple.JSONArray) finalObj.get("contour");

		for (int i = 0; i < finalContourPng.size(); i++) {
			param.put("contour_img_path_" + (i + 1), finalContourPng.get(i));
		}

		org.json.simple.JSONArray finaldesignPng = (org.json.simple.JSONArray) finalObj.get("design");

		for (int i = 0; i < finaldesignPng.size(); i++) {
			param.put("design_img_path_" + (i + 1), finaldesignPng.get(i));
		}

		param.put("ss_user_no", userInfoVo.getUserNo());

		doeService.insertStp07Design(param);

		resultStats.put("resultData", data);

		List selectStp_06 = doeService.selectListFormulaStp_06(param);
		// Step3(CQAs), Step6(개별 응답값에 대한 목표치)
		resultStats.put("selectStp_03", selectStp_03);
		resultStats.put("selectStp_06", selectStp_06);
		resultJSON.put("resultStats", resultStats);

		System.out.println("api7 최종 resultJSON : " + resultJSON);

		return resultJSON;
	}

	/* STEP6 종료 */

	@RequestMapping(value = "/pharmai/formulation/excelDown.do")
	public void excelDown(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		OutputStream os = response.getOutputStream();

		String pathTemplateName = "template/formu_template.xlsx";

		DataMap params = RequestUtil.getDataMap(request);

		List selectStep6ResultList = doeService.selectStp07result(params);
		List selectStep3ResultList = experimentService.selectListFormulaStp_03(params);
		List selectFormulaStp_06 = doeService.selectListFormulaStp_06(params);

		// System.out.println("excel Down common controller selectStep6ResultList : " +
		// selectStep3ResultList);

		ClassPathResource cpr = new ClassPathResource(pathTemplateName);
		try (InputStream input = new BufferedInputStream(cpr.getInputStream())) {// 1

			response.setHeader("Content-Disposition",
					"atachment; filename=\"pharmaiFormulation_" + System.currentTimeMillis() + ".xlsx\"");

			Context context = new Context(); // context 속성에 컨텍스트명과 엑셀에 쓰일 데이터를 Key & Value로 세팅
			context.putVar("step6ResultList", selectStep6ResultList); // contextName("step6ResultList")은 엑셀 템플릿파일에서
																		// items="컨텍스트명"과 반드시 일치
			context.putVar("step3ResultList", selectStep3ResultList); // contextName("step3ResultList")은 엑셀 템플릿파일에서
																		// items="컨텍스트명"과 반드시 일치
			context.putVar("step3FormulaList", selectFormulaStp_06); // contextName("step3ResultList")은 엑셀 템플릿파일에서
																		// items="컨텍스트명"과 반드시 일치
			context.putVar("title", "PharmAI 합성의약품 제형 결과 Report");

			JxlsHelper.getInstance().processTemplate(input, os, context); // 3

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}

	}

}
