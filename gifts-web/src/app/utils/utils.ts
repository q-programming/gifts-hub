export function getBase64Image(data: String): String {
  return data.replace(/^data:image\/(png|jpg|jpeg);base64,/, "");
}
