package egovframework.bio.formulation.service;

import java.util.List;

import org.springframework.ui.ModelMap;

import egovframework.framework.common.object.DataMap;

public interface BioFormulationService {
	/**
	 * <PRE>
	 * 1. MethodName : selectPageListProject
	 * 2. ClassName  : BioFormulationServiceImpl
	 * 3. Comment   : Formulation 불러오기 (프로젝트 리스트)
	 * 4. 작성자    : hnpark
	 * 5. 작성일    : 2022. 4. 21
	 * </PRE>
	 *   @return String
	 *   @param request
	 *   @param response
	 *   @param model
	 *   @return
	 *   @throws Exception
	 */
	
	List selectPageListProject (ModelMap model,DataMap param) throws Exception;
	
	/**
	 * <PRE>
	 * 1. MethodName : selectTotCntProject
	 * 2. ClassName  : BioFormulationServiceImpl
	 * 3. Comment   : 프로젝트 총 개수 조회
	 * 4. 작성자    : hnpark
	 * 5. 작성일    : 2022. 4. 21
	 * </PRE>
	 *   @param param
	 *   @return
	 *   @throws Exception
	 */
	int selectTotCntProject(DataMap param) throws Exception;
	
	// 바이오의약품 제형 시작하기
	DataMap selectPjtMst(DataMap param) throws Exception;
	
	void updateChoicePrjct(DataMap param) throws Exception;
	
	String selectNextDataExt(DataMap param) throws Exception;
	
	// 바이오의약품 제형 시작하기 > 프로젝트명 입력 후 등록
	void projectNmUpdate(DataMap param) throws Exception;
	
	// 제형 다음 STEP
	DataMap stepChangeFunc(DataMap param) throws Exception;
	
	public void updatePjt_mst(DataMap param) throws Exception;
	
	// 프로젝트 복사
	void copyPrjct(DataMap param) throws Exception;
	
	// 프로젝트 삭제
	public void updatePrjMst(DataMap param) throws Exception;

	/**
	 * step 4 정보를 DB에서 직접 조회하는 서비스
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	List selectApi4RoutesData(DataMap param) throws Exception;
	
	
}
