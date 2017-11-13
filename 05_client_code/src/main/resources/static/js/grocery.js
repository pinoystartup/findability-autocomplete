$(document).ready(function() {

    var options = {
        url: function(q) {
            return "http://searchai.us-west-1.elasticbeanstalk.com/grocery/products_suggest?q=" + q;
        },
        ajaxSettings: { dataType: "json"},
        listLocation: "suggests",
        getValue: "name",
        theme: "square",
        minCharNumber: 2,
        list: {
            maxNumberOfElements: 5,
            hideOnEmptyPhrase: true,
            onClickEvent: function() {},
            onChooseEvent: function() {},
            onKeyEnterEvent: function() {}
        },
        requestDelay: 20,
        template: {
            type: "custom",
            method: function(value, item) {
                var suggests =
                    "<section>"+
                        "<a href='https://www.google.com/search?q="+item.name+"&tbm=isch' target=_blank>"+
                            "<div class='one'>"+"<img src='../img/grocery.jpg' width='44' height='50' style='padding-top:5px;'>"+"</div>"+
                            "<div class='two'>"+
                                "<div class='three'>"+item.name+"</div>"+
                                "<div class='four'>"+item.brand+"</div>"+
                            "</div>" +
                        "</a>"+
                    "</section>";
                return suggests;
            }
        }
    };

    $("#findability-autocomplete").easyAutocomplete(options);

    $("#findability-autocomplete").keypress(function (e) {
        var key = e.which;
        if (key == 13) {
            alert("Value: " + $("#findability-autocomplete").val());
        }
    });
});