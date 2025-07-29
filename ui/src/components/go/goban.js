const place = (ch, x, y) => {

  const ox = 0;
  const oy = this.square_size / 6;

  const text = document.createElementNS("http://www.w3.org/2000/svg", "text");
  text.setAttribute("x", (x + ox).toFixed(0));
  text.setAttribute("y", (y + oy).toFixed(0));
  text.setAttribute("font-size", `${Math.round(text_size)}px`);
  text.setAttribute("font-size", `${Math.round(text_size)}px`);
  text.setAttribute("font-weight", 'bold');
  text.setAttribute("fill",'#444444');

  return text
}

export const PRETTY_COORDINATE_SEQUENCE = 'ABCDEFGHJKLMNOPQRSTUVWXYZ'