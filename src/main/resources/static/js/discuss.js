/*页面加载完后绑定单击事件，与调用like()形式不同，作用相同*/
$(function(){
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});

// 置顶
function setTop() {
    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                // 成功后设置按钮不可用
                $("#topBtn").attr("disabled", "disabled");
            } else {
                // 失败提示错误消息
                alert(data.msg);
            }
        }
    );
}

// 加精
function setWonderful() {
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $("#wonderfulBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 删除
function setDelete() {
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                // 删除后直接跳转到首页
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.msg);
            }
        }
    );
}


// 点赞
function like(btn,entityType,entityId,entityUserId,postId) {
    $.post(
        /*访问路径*/
        CONTEXT_PATH + "/like",
        /*传入数据*/
        {"entityType":entityType, "entityId": entityId, "entityUserId": entityUserId, "postId": postId},
        /*成功后的回调函数*/
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                /*点赞成功*/
                $(btn).children("b").text(data.likeStatus == 1 ? "已赞" : "赞");
                $(btn).children("i").text(data.likeCount);
            } else {
                alert(data.msg);
            }
        }
    );
}

