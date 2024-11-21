package egovframework.pharmai.formulation.service;

import java.util.List;

import egovframework.framework.common.object.DataMap;

public interface ExcipientService {

	List selectStp03(DataMap param) throws Exception;

	void insertStep3(DataMap param) throws Exception;
	
	/**
	 * <PRE>
	 * 1. MethodName : selectListFormulationCombo
	 * 2. ClassName  : ExcipientService
	 * 3. Comment   : 부형제 추천
	 * 4. 작성자    : hnpark
	 * 5. 작성일    : 2022. 10. 12.
	 * </PRE>
	 *   @return DataMap
	 *   @param param
	 *   @return
	 */
	List selectListFormulationCombo(DataMap param) throws Exception;
}
