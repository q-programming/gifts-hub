/**
 * Util class with functions shared across different controllers
 */
/**
 * Convert image element into base64 image
 * @param imgElem element to be converted
 * @returns {string}
 */
function getBase64Image(imgElem) {
    var canvas = document.createElement("canvas");
    canvas.width = imgElem.clientWidth;
    canvas.height = imgElem.clientHeight;
    var ctx = canvas.getContext("2d");
    ctx.drawImage(imgElem, 0, 0);
    var dataURL = canvas.toDataURL("image/png");
    return dataURL.replace(/^data:image\/(png|jpg);base64,/, "");
}