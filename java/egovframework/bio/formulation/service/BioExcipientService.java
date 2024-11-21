package egovframework.bio.formulation.service;

import java.util.List;

import egovframework.framework.common.object.DataMap;

public interface BioExcipientService {

	// step3
	List selectStp03(DataMap param) throws Exception;
	
	// step3에서 입력한 사용범위를 넘겨줌
	void insertStep3(DataMap param) throws Exception;
	
	/**
	 * <PRE>
	 * 1. MethodName : selectListFormulationCombo
	 * 2. ClassName  : BioExcipientService
	 * 3. Comment   : 부형제 추천
	 * 4. 작성자    : hnpark
	 * 5. 작성일    : 2022. 10. 13.
	 * </PRE>
	 *   @return DataMap
	 *   @param param
	 *   @return
	 */
	List selectListFormulationCombo(DataMap param) throws Exception;
}
