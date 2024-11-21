<%@ page contentType="text/html;charset=euc-kr" %>
<%@ page language="java" import="Kisinfo.Check.IPIN2Client" %>

<%
	/********************************************************************************************************************************************
		NICE������ Copyright(c) KOREA INFOMATION SERVICE INC. ALL RIGHTS RESERVED

		���񽺸� : IPIN �����ֹι�ȣ ����
		�������� : IPIN �����ֹι�ȣ ���� ȣ�� ������
	*********************************************************************************************************************************************/

	String sSiteCode	= "FQ00";		//  NICE���������� �߱��� IPIN ���� ����Ʈ�ڵ�
	String sSitePw		= "seoullabor0308!";		//  NICE���������� �߱��� IPIN ���� ����Ʈ�н�����


	/*
	�� sReturnURL ������ ���� ����  ����������������������������������������������������������������������������������������������������������
		��ȣȭ�� ������� �����͸� ���Ϲ��� URL�� �������ݺ��� Ǯ�ּҷ� �������ּ���.

		* URL�� �ݵ�� �������ݺ��� �Է��ؾ� �ϸ� �ܺο��� ������ ������ �ּҿ��� �մϴ�.
		* ��� ���������� �� ipin_process �������� ������� �����͸� ���Ϲ޴� �������Դϴ�.

		�� - http://www.test.co.kr/ipin_process.jsp
			 https://www.test.kr:4343/ipin_process.jsp
	������������������������������������������������������������������������������������������������������������������������������������������
	*/
	String sReturnURL   = "http://localhost:8070/admin/userCrtfc/ipin_process.jsp";


	/*
	�� sCPRequest ������ ���� ����  ����������������������������������������������������������������������������������������������������������
		CP��û��ȣ�� �߰����� ����ó���� ���� �����Դϴ�. ���� �� ������� �����Ϳ� �Բ� ���޵˴ϴ�.
		���ǿ� ����� ���� ���� ������ �������� �˻��ϰų�, ����� Ư�������� �̿��� �� �ֽ��ϴ�.
		������ �˻�� ������ �ʼ����� ó���� �ƴϸ� ������ ���� �ǰ� �����Դϴ�.

		+ CP��û��ȣ ���� ���
			1. ��翡�� ������ ���� ����
			2. �ͻ翡�� ���Ƿ� ����(�ִ� 30byte)
	������������������������������������������������������������������������������������������������������������������������������������������
	*/
	String sCPRequest = "";


	// ��ⰴü ����
	IPIN2Client pClient = new IPIN2Client();

	// CP��û��ȣ ����
	sCPRequest = pClient.getRequestNO(sSiteCode);

	// CP��û��ȣ ���ǿ� ����
	// : ����� ���� ipin_result ���������� ������ ������ �˻翡 �̿�˴ϴ�.
	session.setAttribute("CPREQUEST" , sCPRequest);


	// ������û ��ȣȭ ������ ����
	int iRtn = pClient.fnRequest(sSiteCode, sSitePw, sCPRequest, sReturnURL);

	String sEncData		= "";	// ������û ��ȣȭ ������
	String sRtnMsg		= "";	// ó����� �޼���

	// ��ȣȭ ó������ڵ忡 ���� ó��
	if (iRtn == 0)
	{
        // ������û ��ȣȭ ������ ����
		sEncData = pClient.getCipherData();
		sRtnMsg = "���� ó���Ǿ����ϴ�.";
	}
	else if (iRtn == -1)
	{
		sRtnMsg = "��ȣȭ �ý��� ���� : �ͻ� ���� ȯ�濡 �´� ����� �̿����ֽʽÿ�.<br>������ ���ӵǴ� ��� iRtn ��, ���� ȯ������, ����Ʈ�ڵ带 ������ �����ֽñ� �ٶ��ϴ�.";
	}
	else if (iRtn == -2)
	{
		sRtnMsg = "��ȣȭ ó�� ���� : �ֽ� ����� �̿����ֽʽÿ�. ������ ���ӵǴ� ��� iRtn ��, ���� ȯ������, ����Ʈ�ڵ带 ������ �����ֽñ� �ٶ��ϴ�.";
	}
	else if (iRtn == -9)
	{
		sRtnMsg = "�Է� ���� ���� : ��ȣȭ �Լ��� �Էµ� �Ķ���� ���� Ȯ�����ֽʽÿ�.<br>������ ���ӵǴ� ���, �Լ� ���� ���� �� �Ķ���� ���� �α׷� ����� �߼����ֽñ� �ٶ��ϴ�.";
	}
	else
	{
		sRtnMsg = "��Ÿ ����: iRtn ���� ������ ���üҽ��� �߼����ֽñ� �ٶ��ϴ�.";
	}

%>

<html>
<head>
	<meta charset="EUC-KR">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>NICE������ �����ֹι�ȣ ����</title>
	<script>
		window.name ="Parent_window";

		function fnPopup(){
			window.open('', 'popupIPIN2', 'width=450, height=550, top=100, left=100, fullscreen=no, menubar=no, status=no, toolbar=no, titlebar=yes, location=no, scrollbar=no');
			document.form_ipin.target = "popupIPIN2";
			document.form_ipin.action = "https://cert.vno.co.kr/ipin.cb";
			document.form_ipin.submit();
		}
	</script>
</head>
<body>
	iRtn : <%= iRtn %> - <%= sRtnMsg %><br><br>
	������û ��ȣȭ ������ : [<%= sEncData %>]<br><br>


	<!-- ������ �����ֹι�ȣ ���� �˾� ȣ�� form -->
	<form name="form_ipin" method="post">
		<!-- ��û��� (�ʼ� ������) -->
		<input type="hidden" name="m" value="pubmain">
		<!-- ������û ��ȣȭ ������ -->
		<input type="hidden" name="enc_data" value="<%= sEncData %>">
			<!-- �˾� ȣ�� �̹��� ��ư -->
			<a href="javascript:fnPopup();">
			<img src="http://image.creditbank.co.kr/static/img/vno/new_img/bt_17.gif" width=218 height=40 border=0>
		</a>
	</form>


	<!-- ������ �����ֹι�ȣ ���� �˾� ������� ���� form -->
	<form name="vnoform" method="post">
		<!-- ������� ��ȣȭ ������ -->
		<input type="hidden" name="enc_data">
	</form>
</body>
</html>