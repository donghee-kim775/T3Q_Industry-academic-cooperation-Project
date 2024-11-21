package egovframework.bio.formulation.service;

import java.util.List;

import egovframework.framework.common.object.DataMap;

public interface BioRoutesService {
	
	DataMap selectDosageFormCnt(DataMap param) throws Exception;
	
	List selectListExcipient(DataMap param) throws Exception;
	
	DataMap selectDataStep2(DataMap param) throws Exception;

	// step2 insert문
	void insertFormulaStp_02(DataMap param ) throws Exception;
	
	// step2에서 입력한 데이터를 불러옴
	DataMap selectStp02(DataMap param) throws Exception;
	
	/**
	 * <PRE>
	 * 1. MethodName : selectListRoutesCombo
	 * 2. ClassName  : RoutesService
	 * 3. Comment   : 투여경로에 따른 제형 추천 
	 * 4. 작성자    : hnpark
	 * 5. 작성일    : 2022. 10. 11.
	 * </PRE>
	 *   @return DataMap
	 *   @param param
	 *   @return
	 */
	List selectListRoutesCombo(DataMap param) throws Exception;
}
