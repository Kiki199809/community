/*绑定提交事件和获得焦点事件，处理两次密码输入不一致问题*/
$(function(){
	$("form").submit(check_data);
	$("input").focus(clear_error);
});

function check_data() {
	var pwd1 = $("#new-password").val();
	var pwd2 = $("#confirm-password").val();
	if(pwd1 != pwd2) {
		$("#confirm-password").addClass("is-invalid");
		return false;
	}
	return true;
}

function clear_error() {
	$(this).removeClass("is-invalid");
}

/*给表单提交绑定提交事件，提交数据到七牛云*/
$(function(){
	$("#uploadForm").submit(upload);
});

function upload() {
	/*$.post()是对$.ajax()的简化*/
	$.ajax({
		url: "http://upload-z2.qiniup.com",
		method: "post",
		/*不把表单数据转化为字符串*/
		processData: false,
		/*不然jQuery设置类型*/
		contentType: false,
		data: new FormData($("#uploadForm")[0]),
		success: function(data) {
			// 成功上传后再进行异步更新头像访问路径
			if(data && data.code == 0) {
				// 更新头像访问路径
				$.post(
					CONTEXT_PATH + "/user/header/url",
					{"fileName":$("input[name='key']").val()},
					function(data) {
						data = $.parseJSON(data);
						if(data.code == 0) {
							// 刷新当前页面
							window.location.reload();
						} else {
							alert(data.msg);
						}
					}
				);
			} else {
				alert("上传失败!");
			}
		}
	});
	return false;
}