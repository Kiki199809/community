/*绑定单击事件，调用publish函数*/
$(function(){
	$("#publishBtn").click(publish);
});

function publish() {

	/*隐藏发布框*/
	$("#publishModal").modal("hide");

	// 发送AJAX请求之前，将CSEF令牌设置到请求的消息头中
	// var token = $("meta[name='_csrf']").attr("content");
	// var header = $("meta[name='_csrf_header']").attr("content");
	// $(document).ajaxSend(function (e, xhr, token) {
	// 	xhr.setRequestHeader(header, token);
	// });

	// 获取标题和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	// 发送异步请求（POST）
	$.post(
		/*url*/
		CONTEXT_PATH + "/discuss/add",
		/*data*/
		{"title": title, "context": content},
		/*成功后的回调函数*/
		function (data) {
			data = $.parseJSON(data);
			// 输入提示消息
			$("#hintBody").text(data.msg);
			// 显示提示消息
			$("#hintBody").modal("show");
			// 2s后，自动隐藏提示消息
			setTimeout(function () {
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
	setTimeout(function () {
		$("#hintModal").modal("hide");
	}, 2000);
}