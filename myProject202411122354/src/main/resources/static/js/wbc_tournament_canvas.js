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
// 国旗 Image キャッシュ（ちらつかない方式）
// ==============================
const FLAGS = (() => {
  const srcMap = {
    // Group A / 強豪
    USA: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/us.jpg",
    JAPAN: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/jp.jpg",
    KOREA: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/kr.jpg",
    AUSTRALIA: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/au.jpg",
    CANADA: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/ca.jpg",
    MEXICO: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/mx.jpg",

    // 中南米
    BRAZIL: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/br.jpg",
    ITALY: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/it.jpg",
    "PUERTO RICO": "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/pr.jpg",
    CUBA: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/cu.jpg",
    COLOMBIA: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/co.jpg",
    PANAMA: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/pa.jpg",
    VENEZUELA: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/ve.jpg",
    "DOMINICAN REPUBLIC": "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/do.jpg",

    // 欧州・その他
    NETHERLANDS: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/nl.jpg",
    ISRAEL: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/il.jpg",
    NICARAGUA: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/ni.jpg",
    CZECH_REPUBLIC: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/cz.jpg",
    "CHINESE TAIPEI": "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/tw.jpg",
    "UNITED KINGDOM": "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/gb.jpg"
  };

  const images = {};
  Object.keys(srcMap).forEach(k => {
    const img = new Image();
    img.onload = () => redraw(); // ★ロード完了で再描画（既存仕様）
    img.src = srcMap[k];
    images[k] = img;
  });

  return images;
})();

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
  // 試合表示行生成
  // ==============================
  function matchLines(m) {
    if (!m) return ["未定", "vs", "未定"];

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
// 国旗＋チーム名（左寄せ）【最終安定版】
// ==============================
function drawTeamWithFlagLeft(x, y, teamText) {
  if (!teamText) return;

  ctx.font = "12px sans-serif";
  ctx.textAlign = "left";

  // ★ 空白・全角・NBSP をすべて正規化
  const normalized = teamText
    .replace(/\u00A0/g, " ") // NBSP
    .replace(/\s+/g, " ")
    .trim();

  // ★ 最後が数字ならスコアとして分離
  const match = normalized.match(/^(.*?)(?:\s+(\d+))?$/);
  const team = match?.[1]?.trim();
  const score = match?.[2] ?? "";

  const flag = FLAGS[team];
  const FLAG_W = 26;
  const FLAG_H = 18;
  const GAP = 6;

  if (flag && flag.complete && flag.naturalWidth > 0) {
    ctx.drawImage(flag, x, y - FLAG_H / 2, FLAG_W, FLAG_H);
    ctx.fillText(`${team}${score ? " " + score : ""}`, x + FLAG_W + GAP, y);
  } else {
    // フォールバック（必ず文字は出す）
    ctx.fillText(normalized, x, y);
  }
}



// ==============================
// 国旗＋チーム名（中央揃え）
// ==============================
function drawTeamWithFlagCentered(cx, y, teamText) {
  if (!teamText) return;

  ctx.font = "12px sans-serif";
  ctx.textAlign = "left"; // ★手動配置するので left 固定

  // teamText: "USA 7" / "PUERTO RICO 3" など
  const parts = teamText.split(" ");
  const team = parts.slice(0, -1).join(" "); // 複数単語対応
  const score = parts[parts.length - 1];

  const text = `${team} ${score}`;

  const flag = FLAGS[team];
  const FLAG_W = flag ? 26 : 0;
  const FLAG_H = 18;
  const GAP = flag ? 6 : 0;

  // テキスト幅
  const textWidth = ctx.measureText(text).width;

  // 全体幅（国旗 + gap + テキスト）
  const totalWidth = FLAG_W + GAP + textWidth;

  // 左端X（中央から逆算）
  const startX = cx - totalWidth / 2;

  // 国旗
  if (flag && flag.complete && flag.naturalWidth > 0) {
    ctx.drawImage(flag, startX, y - FLAG_H / 2, FLAG_W, FLAG_H);
  }

  // テキスト
  ctx.fillText(text, startX + FLAG_W + GAP, y);
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
  // 再描画本体（★唯一の追加構造）
  // ==============================
  function redraw() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // 優勝
    drawBox(
      CENTER_X - BOX_W / 2,
      Y_CHAMP,
      BOX_W,
      BOX_H,
      "優勝",
      [tournament.FINAL?.[1]?.winnerTeam ?? "未定"]
    );

    // 決勝
    drawBox(
      CENTER_X - BOX_W / 2,
      Y_FINAL,
      BOX_W,
      BOX_H,
      "決勝",
      ["'26/3/17(火)", ...matchLines(tournament.FINAL?.[1])]
    );

    vLine(CENTER_X, Y_CHAMP + BOX_H, Y_FINAL);

    // 準決勝
    const SF_LEFT_X  = CENTER_X - GAP_X - BOX_W / 2;
    const SF_RIGHT_X = CENTER_X + GAP_X - BOX_W / 2;

    drawBox(
      SF_LEFT_X,
      Y_SF,
      BOX_W,
      BOX_H,
      "① 準決勝",
      ["'26/3/15(日)", ...matchLines(tournament.SF?.[1])]
    );

    drawBox(
      SF_RIGHT_X,
      Y_SF,
      BOX_W,
      BOX_H,
      "② 準決勝",
      ["'26/3/16(月)", ...matchLines(tournament.SF?.[2])]
    );

    vLine(CENTER_X, Y_FINAL + BOX_H, Y_FINAL + BOX_H + CONNECT);
    hLine(SF_LEFT_X + BOX_W / 2, SF_RIGHT_X + BOX_W / 2, Y_FINAL + BOX_H + CONNECT);
    vLine(SF_LEFT_X + BOX_W / 2,  Y_FINAL + BOX_H + CONNECT, Y_SF);
    vLine(SF_RIGHT_X + BOX_W / 2, Y_FINAL + BOX_H + CONNECT, Y_SF);

    // 準々決勝
    const QF_L1_X = SF_LEFT_X  - BOX_W / 2;
    const QF_L2_X = SF_LEFT_X  + BOX_W / 2;
    const QF_R1_X = SF_RIGHT_X - BOX_W / 2;
    const QF_R2_X = SF_RIGHT_X + BOX_W / 2;

    // QF①（国旗付き）
// ==============================
// 準々決勝（QF①〜④ 国旗付き・統一方式）
// ==============================

// ==============================
// 準々決勝（QF①〜④ 国旗付き・中央揃え統一）
// ==============================

const qf1 = tournament.QF?.[1];
drawBox(QF_L1_X, Y_QF, BOX_W, BOX_H, "① 準々決勝", [
  "",
  "vs",
  "",
  qf1?.winnerTeam ? `勝者：${qf1.winnerTeam}` : null
]);
if (qf1) {
  drawTeamWithFlagCentered(
    QF_L1_X + BOX_W / 2,
    Y_QF + 45,
    `${qf1.homeTeam} ${qf1.homeScore ?? ""}`
  );
  drawTeamWithFlagCentered(
    QF_L1_X + BOX_W / 2,
    Y_QF + 81,
    `${qf1.awayTeam} ${qf1.awayScore ?? ""}`
  );
}

const qf2 = tournament.QF?.[2];
drawBox(QF_L2_X, Y_QF, BOX_W, BOX_H, "② 準々決勝", [
  "",
  "vs",
  "",
  qf2?.winnerTeam ? `勝者：${qf2.winnerTeam}` : null
]);
if (qf2) {
  drawTeamWithFlagCentered(
    QF_L2_X + BOX_W / 2,
    Y_QF + 45,
    `${qf2.homeTeam} ${qf2.homeScore ?? ""}`
  );
  drawTeamWithFlagCentered(
    QF_L2_X + BOX_W / 2,
    Y_QF + 81,
    `${qf2.awayTeam} ${qf2.awayScore ?? ""}`
  );
}

const qf3 = tournament.QF?.[3];
drawBox(QF_R1_X, Y_QF, BOX_W, BOX_H, "③ 準々決勝", [
  "",
  "vs",
  "",
  qf3?.winnerTeam ? `勝者：${qf3.winnerTeam}` : null
]);
if (qf3) {
  drawTeamWithFlagCentered(
    QF_R1_X + BOX_W / 2,
    Y_QF + 45,
    `${qf3.homeTeam} ${qf3.homeScore ?? ""}`
  );
  drawTeamWithFlagCentered(
    QF_R1_X + BOX_W / 2,
    Y_QF + 81,
    `${qf3.awayTeam} ${qf3.awayScore ?? ""}`
  );
}

const qf4 = tournament.QF?.[4];
drawBox(QF_R2_X, Y_QF, BOX_W, BOX_H, "④ 準々決勝", [
  "",
  "vs",
  "",
  qf4?.winnerTeam ? `勝者：${qf4.winnerTeam}` : null
]);
if (qf4) {
  drawTeamWithFlagCentered(
    QF_R2_X + BOX_W / 2,
    Y_QF + 45,
    `${qf4.homeTeam} ${qf4.homeScore ?? ""}`
  );
  drawTeamWithFlagCentered(
    QF_R2_X + BOX_W / 2,
    Y_QF + 81,
    `${qf4.awayTeam} ${qf4.awayScore ?? ""}`
  );
}

    // 接続線
    const QF_LEFT_CENTER_1 = QF_L1_X + BOX_W / 2;
    const QF_LEFT_CENTER_2 = QF_L2_X + BOX_W / 2;
    const SF_LEFT_CENTER  = SF_LEFT_X + BOX_W / 2;

    vLine(QF_LEFT_CENTER_1, Y_QF, Y_QF - CONNECT);
    vLine(QF_LEFT_CENTER_2, Y_QF, Y_QF - CONNECT);
    hLine(QF_LEFT_CENTER_1, QF_LEFT_CENTER_2, Y_QF - CONNECT);
    vLine(SF_LEFT_CENTER, Y_QF - CONNECT, Y_SF + BOX_H);
    hLine(QF_LEFT_CENTER_1, SF_LEFT_CENTER, Y_QF - CONNECT);

    const QF_RIGHT_CENTER_1 = QF_R1_X + BOX_W / 2;
    const QF_RIGHT_CENTER_2 = QF_R2_X + BOX_W / 2;
    const SF_RIGHT_CENTER  = SF_RIGHT_X + BOX_W / 2;

    vLine(QF_RIGHT_CENTER_1, Y_QF, Y_QF - CONNECT);
    vLine(QF_RIGHT_CENTER_2, Y_QF, Y_QF - CONNECT);
    hLine(QF_RIGHT_CENTER_1, QF_RIGHT_CENTER_2, Y_QF - CONNECT);
    vLine(SF_RIGHT_CENTER, Y_QF - CONNECT, Y_SF + BOX_H);
    hLine(QF_RIGHT_CENTER_2, SF_RIGHT_CENTER, Y_QF - CONNECT);
  }

  // 初回描画
  redraw();
});
