$(function() {
    $("body").on("click", "div.custom", function(event) {
        $(this).animate({
            width: "70%",
            opacity: 0.4,
            marginLeft: "0.6in",
            fontSize: "3em",
            borderWidth: "10px"
        }, 1500 );
    })
});