/*声明工程路径前缀*/
var CONTEXT_PATH = "/community";

window.alert = function(message) {
	if(!$(".alert-box").length) {
		$("body").append(
			'<div class="modal alert-box" tabindex="-1" role="dialog">'+
				'<div class="modal-dialog" role="document">'+
				'<div class="modal-content">'+
					'<div class="modal-header">'+
						'<h5 class="modal-title">提示</h5>'+
						'<button type="button" class="close" data-dismiss="modal" aria-label="Close">'+
							'<span aria-hidden="true">&times;</span>'+
						'</button>'+
					'</div>'+
					'<div class="modal-body">'+
						'<p></p>'+
					'</div>'+
					'<div class="modal-footer">'+
						'<button type="button" class="btn btn-secondary" data-dismiss="modal">确定</button>'+
					'</div>'+
					'</div>'+
				'</div>'+
			'</div>'
		);
	}

    var h = $(".alert-box").height();
	var y = h / 2 - 100;
	if(h > 600) y -= 100;
    $(".alert-box .modal-dialog").css("margin", (y < 0 ? 0 : y) + "px auto");
	
	$(".alert-box .modal-body p").text(message);
	$(".alert-box").modal("show");
}

/*通用的ajax请求完成时调用的函数，在回调函数之前。
这个函数会得到两个参数：XMLHttpRequest对象和一个描述请求成功的类型的字符串。
当请求完成时调用函数，即status==404、403、302...。*/
/*
$.ajaxSetup( {
	complete: function (xhr, status) {
		//拦截器实现超时跳转到登录页面
		// 通过xhr取得响应头
		var REDIRECT = xhr.getResponseHeader("REDIRECT");
		//如果响应头中包含 REDIRECT 则说明是拦截器返回的
		if (REDIRECT == "REDIRECT")
		{
			var win = window;
			while (win != win.top)
			{
				win = win.top;
			}
			//重新跳转到拦截器中的路径
			win.location.href = xhr.getResponseHeader("CONTEXTPATH");
		}
	}
});*/
