document.addEventListener("DOMContentLoaded", () => {
  const canvas = document.getElementById("wbc-tournament-canvas");
  if (!canvas) return;

  const ctx = canvas.getContext("2d");

  // ==============================
  // Canvas設定
  // ==============================
  canvas.width = 1100;
  canvas.height = 900;

  ctx.strokeStyle = "#000";
  ctx.lineWidth = 2;
  ctx.font = "14px sans-serif";
  ctx.textAlign = "center";
  ctx.textBaseline = "middle";

  // ==============================
  // 共通描画関数
  // ==============================
  function drawBox(x, y, w, h, title, lines = []) {
    ctx.strokeRect(x, y, w, h);

    ctx.font = "bold 14px sans-serif";
    ctx.fillText(title, x + w / 2, y + 18);

    ctx.font = "12px sans-serif";
    lines.forEach((t, i) => {
      ctx.fillText(t, x + w / 2, y + 45 + i * 18);
    });
  }

  function vLine(x, y1, y2) {
    ctx.beginPath();
    ctx.moveTo(x, y1);
    ctx.lineTo(x, y2);
    ctx.stroke();
  }

  function hLine(x1, x2, y) {
    ctx.beginPath();
    ctx.moveTo(x1, y);
    ctx.lineTo(x2, y);
    ctx.stroke();
  }

  // ==============================
  // レイアウト定義（微調整済み）
  // ==============================
  const CENTER_X = 550;
  const BOX_W = 210;
  const BOX_H = 130;

  // 🔽 上に余白を作る
  const TOP_MARGIN = 60;

  const Y_CHAMP = TOP_MARGIN + 60;
  const Y_FINAL = TOP_MARGIN + 200;
  const Y_SF    = TOP_MARGIN + 400;
  const Y_QF    = TOP_MARGIN + 590;

  const GAP_X = 280;

  // 🔽 吹き出し長さを統一
  const CONNECT = 30;

  // ==============================
  // 優勝
  // ==============================
  drawBox(
    CENTER_X - BOX_W / 2,
    Y_CHAMP,
    BOX_W,
    BOX_H,
    "優勝",
    ["決勝 勝チーム"]
  );

  // ==============================
  // 決勝
  // ==============================
  drawBox(
    CENTER_X - BOX_W / 2,
    Y_FINAL,
    BOX_W,
    BOX_H,
    "決勝",
    ["'26/3/17(火)", "①準決勝 勝者", "vs", "②準決勝 勝者"]
  );

  // 優勝 → 決勝
  vLine(CENTER_X, Y_CHAMP + BOX_H, Y_FINAL);

  // ==============================
  // 準決勝
  // ==============================
  const SF_LEFT_X  = CENTER_X - GAP_X - BOX_W / 2;
  const SF_RIGHT_X = CENTER_X + GAP_X - BOX_W / 2;

  drawBox(
    SF_LEFT_X,
    Y_SF,
    BOX_W,
    BOX_H,
    "① 準決勝",
    ["'26/3/15(日)", "①準々決勝 勝者", "vs", "②準々決勝 勝者"]
  );

  drawBox(
    SF_RIGHT_X,
    Y_SF,
    BOX_W,
    BOX_H,
    "② 準決勝",
    ["'26/3/16(月)", "③準々決勝 勝者", "vs", "④準々決勝 勝者"]
  );

  // 決勝 → 準決勝（T字・均等）
  vLine(CENTER_X, Y_FINAL + BOX_H, Y_FINAL + BOX_H + CONNECT);
  hLine(
    SF_LEFT_X + BOX_W / 2,
    SF_RIGHT_X + BOX_W / 2,
    Y_FINAL + BOX_H + CONNECT
  );
  vLine(SF_LEFT_X + BOX_W / 2,  Y_FINAL + BOX_H + CONNECT, Y_SF);
  vLine(SF_RIGHT_X + BOX_W / 2, Y_FINAL + BOX_H + CONNECT, Y_SF);

  // ==============================
  // 準々決勝
  // ==============================
  const QF_L1_X = SF_LEFT_X  - BOX_W / 2;
  const QF_L2_X = SF_LEFT_X  + BOX_W / 2;
  const QF_R1_X = SF_RIGHT_X - BOX_W / 2;
  const QF_R2_X = SF_RIGHT_X + BOX_W / 2;

  drawBox(QF_L1_X, Y_QF, BOX_W, BOX_H, "① 準々決勝", ["'26/3/13(金)", "POOL A 2位", "vs", "POOL B 1位"]);
  drawBox(QF_L2_X, Y_QF, BOX_W, BOX_H, "② 準々決勝", ["'26/3/14(土)", "POOL B 2位", "vs", "POOL A 1位"]);

  drawBox(QF_R1_X, Y_QF, BOX_W, BOX_H, "③ 準々決勝", ["'26/3/13(金)", "POOL C 2位", "vs", "POOL D 1位"]);
  drawBox(QF_R2_X, Y_QF, BOX_W, BOX_H, "④ 準々決勝", ["'26/3/14(土)", "POOL D 2位", "vs", "POOL C 1位"]);

  // 準決勝 → 準々決勝（左・均等）
  vLine(SF_LEFT_X + BOX_W / 2, Y_SF + BOX_H, Y_SF + BOX_H + CONNECT);
  hLine(QF_L1_X + BOX_W / 2, QF_L2_X + BOX_W / 2, Y_SF + BOX_H + CONNECT);
  vLine(QF_L1_X + BOX_W / 2, Y_SF + BOX_H + CONNECT, Y_QF);
  vLine(QF_L2_X + BOX_W / 2, Y_SF + BOX_H + CONNECT, Y_QF);

  // 準決勝 → 準々決勝（右・均等）
  vLine(SF_RIGHT_X + BOX_W / 2, Y_SF + BOX_H, Y_SF + BOX_H + CONNECT);
  hLine(QF_R1_X + BOX_W / 2, QF_R2_X + BOX_W / 2, Y_SF + BOX_H + CONNECT);
  vLine(QF_R1_X + BOX_W / 2, Y_SF + BOX_H + CONNECT, Y_QF);
  vLine(QF_R2_X + BOX_W / 2, Y_SF + BOX_H + CONNECT, Y_QF);
});
