document.addEventListener("DOMContentLoaded", async () => {
  const canvas = document.getElementById("wbc-tournament-canvas");
  if (!canvas) return;

  const ctx = canvas.getContext("2d");

  // ==============================
  // DBデータ取得
  // ==============================
  const res = await fetch("/api/wbc/tournament?year=2026");
  const matches = await res.json();

  const tournament = {};
  matches.forEach(m => {
    if (!tournament[m.round]) tournament[m.round] = {};
    tournament[m.round][m.matchNo] = m;
  });

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
      if (t) ctx.fillText(t, x + w / 2, y + 45 + i * 18);
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
  // レイアウト定義（確定）
  // ==============================
  const CENTER_X = 550;
  const BOX_W = 210;
  const BOX_H = 130;
  const TOP_MARGIN = 60;

  const Y_CHAMP = TOP_MARGIN + 60;
  const Y_FINAL = TOP_MARGIN + 200;
  const Y_SF    = TOP_MARGIN + 400;
  const Y_QF    = TOP_MARGIN + 590;

  const GAP_X = 280;
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
    [tournament.FINAL?.[1]?.winnerTeam ?? "未定"]
  );

  // ==============================
  // 決勝
  // ==============================
  const final = tournament.FINAL?.[1];

  drawBox(
    CENTER_X - BOX_W / 2,
    Y_FINAL,
    BOX_W,
    BOX_H,
    "決勝",
    [
      "'26/3/17(火)",
      final?.homeTeam ?? "未定",
      "vs",
      final?.awayTeam ?? "未定"
    ]
  );

  vLine(CENTER_X, Y_CHAMP + BOX_H, Y_FINAL);

// ==============================
// 準決勝
// ==============================
const SF_LEFT_X  = CENTER_X - GAP_X - BOX_W / 2;
const SF_RIGHT_X = CENTER_X + GAP_X - BOX_W / 2;

// 🔽 BOXは常に描画する（データが無くても）
drawBox(
  SF_LEFT_X,
  Y_SF,
  BOX_W,
  BOX_H,
  "① 準決勝",
  [
    "'26/3/15(日)",
    tournament.SF?.[1]?.homeTeam ?? "未定",
    "vs",
    tournament.SF?.[1]?.awayTeam ?? "未定"
  ]
);

drawBox(
  SF_RIGHT_X,
  Y_SF,
  BOX_W,
  BOX_H,
  "② 準決勝",
  [
    "'26/3/16(月)",
    tournament.SF?.[2]?.homeTeam ?? "未定",
    "vs",
    tournament.SF?.[2]?.awayTeam ?? "未定"
  ]
);

// ==============================
// 決勝 → 準決勝 接続線
// ==============================
vLine(CENTER_X, Y_FINAL + BOX_H, Y_FINAL + BOX_H + CONNECT);
hLine(
  SF_LEFT_X + BOX_W / 2,
  SF_RIGHT_X + BOX_W / 2,
  Y_FINAL + BOX_H + CONNECT
);
vLine(SF_LEFT_X + BOX_W / 2,  Y_FINAL + BOX_H + CONNECT, Y_SF);
vLine(SF_RIGHT_X + BOX_W / 2, Y_FINAL + BOX_H + CONNECT, Y_SF);

  // ==============================
  // 準々決勝（DB反映）
  // ==============================
  const QF_L1_X = SF_LEFT_X  - BOX_W / 2;
  const QF_L2_X = SF_LEFT_X  + BOX_W / 2;
  const QF_R1_X = SF_RIGHT_X - BOX_W / 2;
  const QF_R2_X = SF_RIGHT_X + BOX_W / 2;

  function qfLines(no) {
    const m = tournament.QF?.[no];
    return [
      m?.homeTeam ?? "未定",
      "vs",
      m?.awayTeam ?? "未定",
      m?.winnerTeam ? `勝者：${m.winnerTeam}` : ""
    ];
  }

  drawBox(QF_L1_X, Y_QF, BOX_W, BOX_H, "① 準々決勝", qfLines(1));
  drawBox(QF_L2_X, Y_QF, BOX_W, BOX_H, "② 準々決勝", qfLines(2));
  drawBox(QF_R1_X, Y_QF, BOX_W, BOX_H, "③ 準々決勝", qfLines(3));
  drawBox(QF_R2_X, Y_QF, BOX_W, BOX_H, "④ 準々決勝", qfLines(4));

  vLine(SF_LEFT_X + BOX_W / 2, Y_SF + BOX_H, Y_SF + BOX_H + CONNECT);
  hLine(QF_L1_X + BOX_W / 2, QF_L2_X + BOX_W / 2, Y_SF + BOX_H + CONNECT);
  vLine(QF_L1_X + BOX_W / 2, Y_SF + BOX_H + CONNECT, Y_QF);
  vLine(QF_L2_X + BOX_W / 2, Y_SF + BOX_H + CONNECT, Y_QF);

  vLine(SF_RIGHT_X + BOX_W / 2, Y_SF + BOX_H, Y_SF + BOX_H + CONNECT);
  hLine(QF_R1_X + BOX_W / 2, QF_R2_X + BOX_W / 2, Y_SF + BOX_H + CONNECT);
  vLine(QF_R1_X + BOX_W / 2, Y_SF + BOX_H + CONNECT, Y_QF);
  vLine(QF_R2_X + BOX_W / 2, Y_SF + BOX_H + CONNECT, Y_QF);
});
