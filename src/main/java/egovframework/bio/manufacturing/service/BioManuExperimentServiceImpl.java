package egovframework.bio.manufacturing.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import egovframework.framework.common.dao.CommonMybatisDao;
import egovframework.framework.common.object.DataMap;
import egovframework.framework.common.util.EgovPropertiesUtil;
import egovframework.framework.common.util.EgovWebUtil;
import egovframework.framework.common.util.StringUtil;
import egovframework.framework.common.util.SysUtil;
import egovframework.framework.common.util.file.vo.NtsysFileVO;
import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;

/**
 * <PRE>
 * 1. ClassName :
 * 2. FileName  : BioManuExperimentController.java
 * 3. Package  : egovframework.bio.manufacturing.web
 * 4. Comment  : step8
 * 5. 작성자   : hnpark
 * 6. 작성일   : 2022. 4. 29
 * </PRE>
 */
@Service("bioManuExperimentService")
public class BioManuExperimentServiceImpl extends EgovAbstractServiceImpl implements BioManuExperimentService{
	
	/** commonDao */
	@Resource(name="commonMybatisDao")
    private CommonMybatisDao commonMybatisDao;
	
	// step8 첫 화면 호출
	public List selectMenufacStep08(DataMap param) throws Exception {
		return (List)commonMybatisDao.selectList("pharm.manufacturing.selectMenufacStep08", param);
	}
	
	public int selectManufactStp06List(DataMap param) throws Exception {
		// TODO Auto-generated method stub
		return commonMybatisDao.selectOne("pharm.manufacturing.selectManufactStp06List", param);
	}
	
	// step8 save 기능
	public void insertMenufacStep08(DataMap param) throws Exception {
		// TODO Auto-generated method stub

		List exprHeader = param.getList("exprHeader");
		for(int i = 0; i< exprHeader.size(); i++ ) {
			param.put("c" + (i + 1), exprHeader.get(i));
		}
		param.put("type", "H");

		commonMybatisDao.insert("pharm.manufacturing.insertMenufacStep08", param);

		int exprLength = Integer.parseInt(param.getString("exprLength"));
		param.put("type", "V");
		for(int i = 0; i < exprLength; i++ ) {
			List exprList = param.getList("exprVal_" + i);
			for(int j = 0; j < exprList.size(); j++ ) {
				param.put("c" + (j + 1), exprList.get(j));
			}
			commonMybatisDao.insert("pharm.manufacturing.insertMenufacStep08", param);
		}

		List stp08List = commonMybatisDao.selectList("pharm.manufacturing.selectMenufacStep08", param);

		File file = null;
		BufferedWriter bufferWriter = null;

		String filePath = EgovPropertiesUtil.getProperty("Globals.fileWebrootPath.manufacturing") + param.getString("prjct_id") + "/api8/csv/";
		String fileName = "api8_experiment.csv";

		File saveFolder = new File(EgovWebUtil.filePathBlackList(filePath));

		//파일 경로에 폴더가 없으면 폴더생성
		if (!saveFolder.exists() || saveFolder.isFile()) {
			saveFolder.mkdirs();
		}


		file = new File(filePath + fileName);

		bufferWriter = new BufferedWriter(new FileWriter(file));

		DataMap data = new DataMap();

		int selectManufactStp06TotYn = commonMybatisDao.selectOne("selectManufactStp06List", param);

		for( int i = 0; i < stp08List.size(); i++) {
			data = (DataMap) stp08List.get(i);
			for(int j = 1 + selectManufactStp06TotYn; j <= 13; j++) {
				String currentData = data.getString("C" + j);
				if(!"".equals(currentData)) {
					bufferWriter.append(currentData);
					String nextData = data.getString("C" + (j + 1));
					if(!"".equals(nextData)) {
						bufferWriter.append(",");
					}
				}
			}
			bufferWriter.newLine();
		}

		bufferWriter.flush();
		bufferWriter.close();

		String doc_id = StringUtil.nvl(param.getString("doc_id"), SysUtil.getDocId());

		NtsysFileVO fvo = new NtsysFileVO();
		fvo.setFileId(SysUtil.getFileId());
		fvo.setDocId(doc_id);
		fvo.setFileRmk("");
		fvo.setFileNm(fileName);
		fvo.setFileRltvPath(EgovPropertiesUtil.getProperty("Globals.fileWebrootPath.manufacturing") + param.getString("prjct_id") + "/api8/csv/");//상대경로
		fvo.setFileAsltPath(filePath); //절대경료
		fvo.setFileSize(file.length());
		fvo.setSsUserId(param.getString("ss_user_no"));
		fvo.setContentType("application/csv");
		fvo.setFileExtNm("csv");

		commonMybatisDao.insert("common.file.insertAttchFile", fvo);

		//INSERT시 CHANGE
    	String step = param.getString("status");
    	int plusCnt = Integer.parseInt(step.substring(step.length()-1, step.length()));
    	param.put("step_cd", "0" + plusCnt);

    	commonMybatisDao.update("pharm.manufacturing.updateMstStep", param);

	}
	
	// api6 호출
	public List selectStp_06_data(DataMap param) throws Exception {
		return commonMybatisDao.selectList("pharm.manufacturing.selectStp_06_data", param);
	}
	
	// step8 실험치 중간저장 기능
	public void temporarySave(DataMap param) throws Exception {
		// TODO Auto-generated method stub

		List exprHeader = param.getList("exprHeader");
		for(int i = 0; i< exprHeader.size(); i++ ) {
			param.put("c" + (i + 1), exprHeader.get(i));
		}
		param.put("type", "H");

		commonMybatisDao.insert("pharm.manufacturing.insertMenufacStep08", param);

		int exprLength = Integer.parseInt(param.getString("exprLength"));
		param.put("type", "V");
		for(int i = 0; i < exprLength; i++ ) {
			List exprList = param.getList("exprVal_" + i);
			for(int j = 0; j < exprList.size(); j++ ) {
				param.put("c" + (j + 1), exprList.get(j));
			}
			commonMybatisDao.insert("pharm.manufacturing.insertMenufacStep08", param);
		}
		
		// 중간저장 후 next 버튼 클릭 시 csv가 생성되지 않는 문제 해결
		List stp08List = commonMybatisDao.selectList("pharm.manufacturing.selectMenufacStep08", param);

		File file = null;
		BufferedWriter bufferWriter = null;

		String filePath = EgovPropertiesUtil.getProperty("Globals.fileWebrootPath.manufacturing") + param.getString("prjct_id") + "/api8/csv/";
		String fileName = "api8_experiment.csv";

		File saveFolder = new File(EgovWebUtil.filePathBlackList(filePath));

		//파일 경로에 폴더가 없으면 폴더생성
		if (!saveFolder.exists() || saveFolder.isFile()) {
			saveFolder.mkdirs();
		}


		file = new File(filePath + fileName);

		bufferWriter = new BufferedWriter(new FileWriter(file));

		DataMap data = new DataMap();

		int selectManufactStp06TotYn = commonMybatisDao.selectOne("selectManufactStp06List", param);

		for( int i = 0; i < stp08List.size(); i++) {
			data = (DataMap) stp08List.get(i);
			for(int j = 1 + selectManufactStp06TotYn; j <= 13; j++) {
				String currentData = data.getString("C" + j);
				if(!"".equals(currentData)) {
					if(!currentData.equals("NULL")) {
						bufferWriter.append(currentData);
					}
					String nextData = data.getString("C" + (j + 1));
					if(!"".equals(nextData)) {
						if(!nextData.equals("NULL")) {
							bufferWriter.append(",");
						}
					}
				}
			}
			bufferWriter.newLine();
		}

		bufferWriter.flush();
		bufferWriter.close();

		String doc_id = StringUtil.nvl(param.getString("doc_id"), SysUtil.getDocId());

		NtsysFileVO fvo = new NtsysFileVO();
		fvo.setFileId(SysUtil.getFileId());
		fvo.setDocId(doc_id);
		fvo.setFileRmk("");
		fvo.setFileNm(fileName);
		fvo.setFileRltvPath(EgovPropertiesUtil.getProperty("Globals.fileWebrootPath.manufacturing") + param.getString("prjct_id") + "/api8/csv/");//상대경로
		fvo.setFileAsltPath(filePath); //절대경료
		fvo.setFileSize(file.length());
		fvo.setSsUserId(param.getString("ss_user_no"));
		fvo.setContentType("application/csv");
		fvo.setFileExtNm("csv");

		commonMybatisDao.insert("common.file.insertAttchFile", fvo);
		


		//INSERT시 CHANGE
    	String step = param.getString("status");
    	int plusCnt = Integer.parseInt(step.substring(step.length()-1, step.length()));
    	param.put("step_cd", "0" + plusCnt);

    	commonMybatisDao.update("pharm.manufacturing.updateMstStep", param);

	}

}
