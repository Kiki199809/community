$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {

	/*隐藏消息输入框*/
	$("#sendModal").modal("hide");

	// 获取内容
	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();
	// 发送异步请求（POST）
	$.post(
		/*url*/
		CONTEXT_PATH + "/letter/send",
		/*data*/
		{"toName":toName,"content":content},
		/*成功后的回调函数*/
		function(data) {
			data = $.parseJSON(data);
			// 输入提示消息
			$("#hintBody").text(data.msg);
			// 显示提示消息
			$("#hintBody").modal("show");
			// 2s后，自动隐藏提示消息
			setTimeout(function(){
				$("#hintBody").modal("hide");
				// 如果添加成功，刷新页面显示新页面
				if (data.code == 0) {
					window.location.reload();
				}
			}, 2000);
		}
	);

	/*显示提示框(包含提示消息)，2s后自动隐藏*/
	$("#hintModal").modal("show");
	setTimeout(function(){
		$("#hintModal").modal("hide");
	}, 2000);
}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}