// All requests are same-origin since the frontend is served by the same
// Ktor server as the API, so no base URL / CORS setup is needed.
const API = "";

let courses = [];
let selectedCourseId = null;

// ---------- Elements ----------
const courseForm = document.getElementById("course-form");
const coursesList = document.getElementById("courses-list");
const courseSelect = document.getElementById("course-select");

const assignmentForm = document.getElementById("assignment-form");
const assignmentsList = document.getElementById("assignments-list");
const progressSummary = document.getElementById("progress-summary");

// ---------- Init ----------
document.addEventListener("DOMContentLoaded", () => {
  loadCourses();
});

// ================== COURSES ==================

async function loadCourses() {
  const res = await fetch(`${API}/courses`);
  courses = await res.json();
  renderCourses();
  renderCourseSelect();
}

function renderCourses() {
  if (courses.length === 0) {
    coursesList.innerHTML = `<div class="empty-state">No courses yet. Add your first one above.</div>`;
    return;
  }

  coursesList.innerHTML = courses.map(c => {
    const pct = c.hoursPerWeek > 0
      ? Math.min(100, (c.hoursCompleted / c.hoursPerWeek) * 100)
      : 0;
    const selected = c.id === selectedCourseId ? "selected" : "";
    return `
      <div class="item-card ${selected}">
        <div class="item-title">${escapeHtml(c.courseName)} <span class="item-meta">(${escapeHtml(c.courseCode)})</span></div>
        <div class="item-meta">${c.hoursCompleted}h / ${c.hoursPerWeek}h this week · ${c.creditUnits} unit(s)</div>
        <div class="progress-bar-track"><div class="progress-bar-fill" style="width:${pct}%"></div></div>
        <div class="item-actions">
          <button onclick="selectCourse('${c.id}')">View Assignments</button>
          <button onclick="logHours('${c.id}')">+ Log Hours</button>
          <button class="danger" onclick="deleteCourse('${c.id}')">Delete</button>
        </div>
      </div>
    `;
  }).join("");
}

function renderCourseSelect() {
  const currentValue = courseSelect.value;
  courseSelect.innerHTML = `<option value="">Select a course first...</option>` +
    courses.map(c => `<option value="${c.id}">${escapeHtml(c.courseName)} (${escapeHtml(c.courseCode)})</option>`).join("");
  if (courses.some(c => c.id === currentValue)) {
    courseSelect.value = currentValue;
  }
}

courseForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const body = {
    courseName: document.getElementById("courseName").value.trim(),
    courseCode: document.getElementById("courseCode").value.trim(),
    hoursPerWeek: Number(document.getElementById("hoursPerWeek").value),
    creditUnits: Number(document.getElementById("creditUnits").value),
    hoursCompleted: 0
  };
  await fetch(`${API}/courses`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });
  courseForm.reset();
  await loadCourses();
});

async function deleteCourse(id) {
  if (!confirm("Delete this course and all its assignments?")) return;
  await fetch(`${API}/courses/${id}`, { method: "DELETE" });
  if (selectedCourseId === id) {
    selectedCourseId = null;
    assignmentsList.innerHTML = "";
    assignmentForm.classList.add("hidden");
    progressSummary.classList.add("hidden");
  }
  await loadCourses();
}

async function logHours(id) {
  const course = courses.find(c => c.id === id);
  if (!course) return;
  const input = prompt(`Log hours studied this week for "${course.courseName}" (current: ${course.hoursCompleted}h):`, course.hoursCompleted);
  if (input === null) return;
  const newHours = Number(input);
  if (isNaN(newHours) || newHours < 0) { alert("Please enter a valid non-negative number."); return; }

  const updated = { ...course, hoursCompleted: newHours };
  await fetch(`${API}/courses/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(updated)
  });
  await loadCourses();
  if (selectedCourseId === id) loadProgress(id);
}

// ================== ASSIGNMENTS ==================

courseSelect.addEventListener("change", () => {
  const id = courseSelect.value;
  if (id) selectCourse(id);
  else {
    selectedCourseId = null;
    assignmentForm.classList.add("hidden");
    progressSummary.classList.add("hidden");
    assignmentsList.innerHTML = "";
    renderCourses();
  }
});

async function selectCourse(id) {
  selectedCourseId = id;
  courseSelect.value = id;
  assignmentForm.classList.remove("hidden");
  progressSummary.classList.remove("hidden");
  renderCourses();
  await Promise.all([loadAssignments(id), loadProgress(id)]);
}

async function loadAssignments(courseId) {
  const res = await fetch(`${API}/assignments/course/${courseId}`);
  const assignments = await res.json();
  renderAssignments(assignments);
}

function renderAssignments(assignments) {
  if (assignments.length === 0) {
    assignmentsList.innerHTML = `<div class="empty-state">No assignments yet for this course.</div>`;
    return;
  }

  assignments.sort((a, b) => a.weekNumber - b.weekNumber);

  assignmentsList.innerHTML = assignments.map(a => {
    let badge = `<span class="badge pending">Pending</span>`;
    if (a.submitted && a.submittedOnTime === true) badge = `<span class="badge done">On time</span>`;
    else if (a.submitted && a.submittedOnTime === false) badge = `<span class="badge late">Late</span>`;
    else if (a.submitted) badge = `<span class="badge done">Submitted</span>`;

    return `
      <div class="item-card">
        <div class="item-title">Week ${a.weekNumber}: ${escapeHtml(a.title)} ${badge}</div>
        <div class="item-meta">Due ${a.dueDate} · ${a.hoursSpent}h spent</div>
        <div class="item-actions">
          ${!a.submitted ? `
            <button onclick="markSubmitted('${a.id}', true)">Mark on time</button>
            <button onclick="markSubmitted('${a.id}', false)">Mark late</button>
          ` : `<button onclick="unmark('${a.id}')">Unmark</button>`}
          <button class="danger" onclick="deleteAssignment('${a.id}')">Delete</button>
        </div>
      </div>
    `;
  }).join("");
}

assignmentForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  if (!selectedCourseId) return;

  const body = {
    courseId: selectedCourseId,
    title: document.getElementById("assignmentTitle").value.trim(),
    weekNumber: Number(document.getElementById("weekNumber").value),
    dueDate: document.getElementById("dueDate").value,
    submitted: false,
    hoursSpent: Number(document.getElementById("hoursSpent").value) || 0
  };

  await fetch(`${API}/assignments`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });

  assignmentForm.reset();
  await loadAssignments(selectedCourseId);
  await loadProgress(selectedCourseId);
});

async function markSubmitted(id, onTime) {
  const res = await fetch(`${API}/assignments/${id}`);
  const assignment = await res.json();
  const updated = { ...assignment, submitted: true, submittedOnTime: onTime };
  await fetch(`${API}/assignments/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(updated)
  });
  await loadAssignments(selectedCourseId);
  await loadProgress(selectedCourseId);
}

async function unmark(id) {
  const res = await fetch(`${API}/assignments/${id}`);
  const assignment = await res.json();
  const updated = { ...assignment, submitted: false, submittedOnTime: null };
  await fetch(`${API}/assignments/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(updated)
  });
  await loadAssignments(selectedCourseId);
  await loadProgress(selectedCourseId);
}

async function deleteAssignment(id) {
  if (!confirm("Delete this assignment?")) return;
  await fetch(`${API}/assignments/${id}`, { method: "DELETE" });
  await loadAssignments(selectedCourseId);
  await loadProgress(selectedCourseId);
}

async function loadProgress(courseId) {
  const res = await fetch(`${API}/courses/${courseId}/progress`);
  if (!res.ok) return;
  const p = await res.json();
  progressSummary.innerHTML = `
    <div class="stat"><strong>${Number(p.weeklyHourProgressPercent).toFixed(0)}%</strong>weekly hours logged</div>
    <div class="stat"><strong>${p.submitted}/${p.totalAssignments}</strong>assignments submitted</div>
    <div class="stat"><strong>${Number(p.onTimeRatePercent).toFixed(0)}%</strong>submitted on time</div>
  `;
}

// ---------- Helpers ----------
function escapeHtml(str) {
  const div = document.createElement("div");
  div.textContent = str ?? "";
  return div.innerHTML;
}
