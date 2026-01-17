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
      if (t != null) ctx.fillText(t, x + w / 2, y + 45 + i * 18);
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
  // 試合表示行生成（★今回の肝）
  // ==============================
  function matchLines(m) {
    if (!m) {
      return ["未定", "vs", "未定"];
    }

    const homeScore = m.homeScore != null ? m.homeScore : "";
    const awayScore = m.awayScore != null ? m.awayScore : "";

    return [
      `${m.homeTeam}　${homeScore}`,
      "vs",
      `${m.awayTeam}　${awayScore}`,
      m.winnerTeam ? `勝者：${m.winnerTeam}` : null
    ];
  }

  // ==============================
  // レイアウト
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
      ...matchLines(final)
    ]
  );

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
    [
      "'26/3/15(日)",
      ...matchLines(tournament.SF?.[1])
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
      ...matchLines(tournament.SF?.[2])
    ]
  );

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

  drawBox(QF_L1_X, Y_QF, BOX_W, BOX_H, "① 準々決勝", matchLines(tournament.QF?.[1]));
  drawBox(QF_L2_X, Y_QF, BOX_W, BOX_H, "② 準々決勝", matchLines(tournament.QF?.[2]));
  drawBox(QF_R1_X, Y_QF, BOX_W, BOX_H, "③ 準々決勝", matchLines(tournament.QF?.[3]));
  drawBox(QF_R2_X, Y_QF, BOX_W, BOX_H, "④ 準々決勝", matchLines(tournament.QF?.[4]));


// ==============================
// 準々決勝 → 準決勝 接続線（★復活）
// ==============================

// 左側（QF①・② → SF①）
const QF_LEFT_CENTER_1 = QF_L1_X + BOX_W / 2;
const QF_LEFT_CENTER_2 = QF_L2_X + BOX_W / 2;
const SF_LEFT_CENTER  = SF_LEFT_X + BOX_W / 2;

// 縦線（QF → 中間）
vLine(QF_LEFT_CENTER_1, Y_QF, Y_QF - CONNECT);
vLine(QF_LEFT_CENTER_2, Y_QF, Y_QF - CONNECT);

// 横線（合流）
hLine(QF_LEFT_CENTER_1, QF_LEFT_CENTER_2, Y_QF - CONNECT);

// 縦線（中間 → SF）
vLine(SF_LEFT_CENTER, Y_QF - CONNECT, Y_SF + BOX_H);

// 横線（SF接続）
hLine(QF_LEFT_CENTER_1, SF_LEFT_CENTER, Y_QF - CONNECT);


// 右側（QF③・④ → SF②）
const QF_RIGHT_CENTER_1 = QF_R1_X + BOX_W / 2;
const QF_RIGHT_CENTER_2 = QF_R2_X + BOX_W / 2;
const SF_RIGHT_CENTER  = SF_RIGHT_X + BOX_W / 2;

// 縦線（QF → 中間）
vLine(QF_RIGHT_CENTER_1, Y_QF, Y_QF - CONNECT);
vLine(QF_RIGHT_CENTER_2, Y_QF, Y_QF - CONNECT);

// 横線（合流）
hLine(QF_RIGHT_CENTER_1, QF_RIGHT_CENTER_2, Y_QF - CONNECT);

// 縦線（中間 → SF）
vLine(SF_RIGHT_CENTER, Y_QF - CONNECT, Y_SF + BOX_H);

// 横線（SF接続）
hLine(QF_RIGHT_CENTER_2, SF_RIGHT_CENTER, Y_QF - CONNECT);



});
