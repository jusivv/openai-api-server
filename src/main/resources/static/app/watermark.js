export default function (text) {
    const canvas = document.createElement('canvas');
    canvas.width = 160
    canvas.height = 160
    const ctx = canvas.getContext("2d")
    if (ctx) {
        ctx.font = "12px serif"
        ctx.fillStyle = "lightgrey"
        ctx.rotate(45 * Math.PI / 180);
        ctx.fillText(text, 8, 8);
    }
    return canvas.toDataURL('image/png');
}