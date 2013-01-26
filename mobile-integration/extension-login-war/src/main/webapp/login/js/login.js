window.onload = function()
    {
        /* Hide the error message after clicking the close button */
        hideError = function(){
            divError = document.getElementById("error-pane");
            divError.style.display="none";
        }

        butClose = document.getElementById("button-close-alert");
        if (butClose != null) {
            butClose.onmousedown = hideError;
        }

        /* Hide the address bar on some mobile devices */
        setTimeout(function () {
            window.scrollTo(0, 0);
        }, 1000);
    }