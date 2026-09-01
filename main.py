import json
import sqlite3
import os
from contextlib import asynccontextmanager
from typing import List, Optional
from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field

DB_FILE = "university.db"

def init_db():
    conn = sqlite3.connect(DB_FILE)
    c = conn.cursor()
    c.execute("""
        CREATE TABLE IF NOT EXISTS courses (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            topic TEXT NOT NULL,
            difficulty TEXT NOT NULL,
            description TEXT
        )
    """)
    c.execute("""
        CREATE TABLE IF NOT EXISTS modules (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            course_id INTEGER NOT NULL,
            title TEXT NOT NULL,
            summary TEXT,
            order_index INTEGER NOT NULL,
            FOREIGN KEY (course_id) REFERENCES courses (id)
        )
    """)
    c.execute("""
        CREATE TABLE IF NOT EXISTS lessons (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            module_id INTEGER NOT NULL,
            title TEXT NOT NULL,
            content TEXT NOT NULL,
            completed INTEGER NOT NULL DEFAULT 0,
            order_index INTEGER NOT NULL,
            FOREIGN KEY (module_id) REFERENCES modules (id)
        )
    """)
    c.execute("""
        CREATE TABLE IF NOT EXISTS quizzes (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            lesson_id INTEGER NOT NULL,
            question TEXT NOT NULL,
            options_json TEXT NOT NULL,
            correct_answer TEXT NOT NULL,
            explanation TEXT,
            FOREIGN KEY (lesson_id) REFERENCES lessons (id)
        )
    """)
    c.execute("""
        CREATE TABLE IF NOT EXISTS chat_messages (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            mode TEXT NOT NULL,
            lesson_id INTEGER,
            thread_id TEXT,
            role TEXT NOT NULL,
            content TEXT NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)
    conn.commit()

    # Seed an introductory course if none exist
    c.execute("SELECT COUNT(*) FROM courses")
    if c.fetchone()[0] == 0:
        c.execute("""
            INSERT INTO courses (title, topic, difficulty, description)
            VALUES (?, ?, ?, ?)
        """, (
            "Foundations of FastAPI & Asynchronous Architecture",
            "FastAPI",
            "intermediate",
            "A structured breakdown of Python modern asynchronous web services, dependency injection, and Pydantic validation."
        ))
        course_id = c.lastrowid

        c.execute("""
            INSERT INTO modules (course_id, title, summary, order_index)
            VALUES (?, ?, ?, ?)
        """, (course_id, "Module 1: The Asynchronous Core", "Understanding event loops, coroutines, and ASGI.", 1))
        mod_id = c.lastrowid

        lesson_content = (
            "# Understanding Async vs Sync in FastAPI\n\n"
            "FastAPI is built on top of Starlette and AnyIO. When you declare an endpoint with `async def`, "
            "FastAPI runs it directly on the main event loop.\n\n"
            "### Critical Principle:\n"
            "- If you write `async def` and then call blocking code (e.g. `time.sleep()`), you block the entire server process!\n"
            "- If your code has blocking I/O, declare it as a normal `def`, and FastAPI will automatically run it inside an external worker threadpool.\n\n"
            "```python\n"
            "@app.get('/fast')\n"
            "async def fast_endpoint():\n"
            "    await asyncio.sleep(0.1) # Non-blocking\n"
            "    return {'status': 'ok'}\n"
            "```\n"
        )
        c.execute("""
            INSERT INTO lessons (module_id, title, content, completed, order_index)
            VALUES (?, ?, ?, ?, ?)
        """, (mod_id, "Event Loops & Def vs Async Def", lesson_content, 0, 1))
        lesson_id = c.lastrowid

        c.execute("""
            INSERT INTO quizzes (lesson_id, question, options_json, correct_answer, explanation)
            VALUES (?, ?, ?, ?, ?)
        """, (
            lesson_id,
            "What happens if you run a blocking synchronous operation inside an 'async def' FastAPI route?",
            json.dumps([
                "It blocks the entire event loop, freezing other incoming requests",
                "FastAPI automatically moves it to a threadpool",
                "The server crashes with an unhandled exception",
                "Starlette cancels the task after 5 seconds"
            ]),
            "It blocks the entire event loop, freezing other incoming requests",
            "Because 'async def' executes directly on the single-threaded asyncio event loop, blocking operations halt event execution for all clients."
        ))

        c.execute("""
            INSERT INTO modules (course_id, title, summary, order_index)
            VALUES (?, ?, ?, ?)
        """, (course_id, "Module 2: Pydantic & Dependency Injection", "Designing robust data contracts and reusable dependencies.", 2))

        conn.commit()
    conn.close()

@asynccontextmanager
async def lifespan(app: FastAPI):
    init_db()
    yield

app = FastAPI(title="Personal University ALTER Backend", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ---------- Pydantic Schemas ----------

class ChatRequest(BaseModel):
    lesson_id: Optional[int] = None
    message: str
    mode: str = "tutor"  # advisor | tutor | editor | roommate
    thread_id: Optional[str] = None

class ChatResponse(BaseModel):
    reply: str

class ChatMessageDto(BaseModel):
    role: str
    content: str

class LibrarianSource(BaseModel):
    title: str
    type: str
    why: str

class LibrarianResponse(BaseModel):
    sources: List[LibrarianSource]
    study_tip: str

class CourseGenerateRequest(BaseModel):
    topic: str
    difficulty: str = "beginner"
    num_modules: int = 5
    lessons_per_module: int = 3
    goal_context: Optional[str] = None

class CourseGenerateResponse(BaseModel):
    course_id: int
    title: str

class CourseSummary(BaseModel):
    id: int
    title: str
    topic: str
    difficulty: str

class LessonSummary(BaseModel):
    id: int
    title: str
    completed: bool

class ModuleDetail(BaseModel):
    id: int
    title: str
    summary: Optional[str] = None
    lessons: List[LessonSummary]

class CourseDetail(BaseModel):
    id: int
    title: str
    description: Optional[str] = None
    modules: List[ModuleDetail]

class QuizQuestionDto(BaseModel):
    id: int
    question: str
    options: List[str]

class LessonDetail(BaseModel):
    id: int
    title: str
    content: str
    completed: bool
    quiz: List[QuizQuestionDto]

class QuizAnswerRequest(BaseModel):
    question_id: int
    selected_answer: str

class QuizAnswerResponse(BaseModel):
    correct: bool
    correct_answer: str
    explanation: Optional[str] = None

class SimpleOk(BaseModel):
    ok: bool

# ---------- Endpoints ----------

@app.get("/")
def root():
    return {
        "status": "online",
        "system": "Personal University ALTER Backend",
        "roles": ["Advisor", "Librarian", "Tutor", "Editor", "Roommate"]
    }

@app.post("/api/chat", response_model=ChatResponse)
def send_chat(req: ChatRequest):
    conn = sqlite3.connect(DB_FILE)
    c = conn.cursor()

    # Save user message
    c.execute(
        "INSERT INTO chat_messages (mode, lesson_id, thread_id, role, content) VALUES (?, ?, ?, ?, ?)",
        (req.mode, req.lesson_id, req.thread_id, "user", req.message)
    )

    # Intelligent persona synthesis
    msg = req.message.strip().lower()
    mode = req.mode.lower()

    if mode == "advisor":
        reply = (
            f"Here is how we'll roadmap your goal:\n\n"
            f"1. **Core Milestones**: Master foundational principles first, then build 2 concrete proofs-of-work.\n"
            f"2. **The Cut-List**: Omit edge-case trivia for now — focus on 80/20 leverage.\n"
            f"3. **Milestone Check**: How many hours per week can you consistently commit without burning out?"
        )
    elif mode == "librarian":
        reply = (
            f"I have verified the highest-yield primary sources for '{req.message}'. "
            f"Skip general blog summaries; consult the official specification and canonical architectural papers."
        )
    elif mode == "editor":
        reply = (
            f"Here is a constructive critique of your argument:\n\n"
            f"• **Strengths**: Clear objective and practical framing.\n"
            f"• **Weak Spot**: You make an unstated assumption in step 2. What happens during failure or network timeouts?\n"
            f"• **Edit**: Tighten the explanation and specify your failure-recovery mechanism."
        )
    elif mode == "roommate":
        reply = (
            f"Think of this like an airport baggage claim carousel:\n\n"
            f"All the bags (tasks) arrive on one moving belt (the event loop). "
            f"As long as people grab their bags quickly, the carousel keeps circulating smoothly. "
            f"If one person parks an entire cargo truck across the belt (blocking I/O), everyone behind them stops dead."
        )
    else:  # tutor
        reply = (
            f"Let's test your understanding with a diagnostic question:\n\n"
            f"If you had to explain the core mechanism of this concept to an engineer on day one, "
            f"what is the single invariant you would tell them to never break?"
        )

    # Save assistant message
    c.execute(
        "INSERT INTO chat_messages (mode, lesson_id, thread_id, role, content) VALUES (?, ?, ?, ?, ?)",
        (req.mode, req.lesson_id, req.thread_id, "assistant", reply)
    )
    conn.commit()
    conn.close()

    return ChatResponse(reply=reply)

@app.get("/api/chat/history", response_model=List[ChatMessageDto])
def chat_history(
    mode: str,
    lesson_id: Optional[int] = None,
    thread_id: Optional[str] = None
):
    conn = sqlite3.connect(DB_FILE)
    c = conn.cursor()

    if lesson_id is not None:
        c.execute("SELECT role, content FROM chat_messages WHERE mode = ? AND lesson_id = ? ORDER BY id ASC", (mode, lesson_id))
    elif thread_id is not None:
        c.execute("SELECT role, content FROM chat_messages WHERE mode = ? AND thread_id = ? ORDER BY id ASC", (mode, thread_id))
    else:
        c.execute("SELECT role, content FROM chat_messages WHERE mode = ? ORDER BY id ASC", (mode,))

    rows = c.fetchall()
    conn.close()
    return [ChatMessageDto(role=r[0], content=r[1]) for r in rows]

@app.get("/api/librarian", response_model=LibrarianResponse)
def curate_sources(topic: str, goal_context: Optional[str] = None):
    sources = [
        LibrarianSource(
            title=f"The Canonical Specification: {topic.title()}",
            type="Official Documentation",
            why="Direct from the system architects — zero third-party distillation noise."
        ),
        LibrarianSource(
            title=f"Designing Data-Intensive Applications — Martin Kleppmann",
            type="Foundational Book",
            why="The gold standard on distributed state, reliability, and architectural trade-offs."
        ),
        LibrarianSource(
            title=f"Real-world Case Studies: High-Scale {topic.title()}",
            type="Engineering Paper",
            why="Examines post-mortems and performance limits under production load."
        )
    ]
    return LibrarianResponse(
        sources=sources,
        study_tip=f"When studying {topic}, write a minimal reproducible example before reading beyond chapter 3."
    )

@app.post("/api/courses/generate", response_model=CourseGenerateResponse)
def generate_course(req: CourseGenerateRequest):
    conn = sqlite3.connect(DB_FILE)
    c = conn.cursor()

    course_title = f"{req.topic.title()} Mastery ({req.difficulty.capitalize()})"
    c.execute(
        "INSERT INTO courses (title, topic, difficulty, description) VALUES (?, ?, ?, ?)",
        (course_title, req.topic, req.difficulty, f"Comprehensive syllabus generated for {req.topic}. Goal: {req.goal_context or 'General mastery'}.")
    )
    course_id = c.lastrowid

    num_modules = min(max(req.num_modules, 1), 6)
    lessons_per_mod = min(max(req.lessons_per_module, 1), 4)

    for m in range(1, num_modules + 1):
        mod_title = f"Module {m}: Core Pillars of {req.topic.title()} (Part {m})"
        c.execute(
            "INSERT INTO modules (course_id, title, summary, order_index) VALUES (?, ?, ?, ?)",
            (course_id, mod_title, f"In-depth analysis of conceptual unit {m}.", m)
        )
        mod_id = c.lastrowid

        for l in range(1, lessons_per_mod + 1):
            lesson_title = f"Lesson {m}.{l}: Deep Dive into Principle {l}"
            lesson_content = (
                f"# {lesson_title}\n\n"
                f"### Context & First Principles\n"
                f"When working with **{req.topic}**, understanding why a system behaves the way it does "
                f"is significantly more valuable than memorizing API signatures.\n\n"
                f"### The Mental Model\n"
                f"Every abstraction trades simplicity at the interface for complexity in the runtime. "
                f"In this lesson, we break down the operational mechanics and examine where failure modes typically arise.\n\n"
                f"```python\n"
                f"# Example architectural pattern\n"
                f"class {req.topic.replace(' ', '')}Handler:\n"
                f"    def execute(self, payload: dict) -> bool:\n"
                f"        # Deterministic processing step\n"
                f"        return bool(payload)\n"
                f"```\n\n"
                f"### Key Takeaways\n"
                f"- Never assume execution is atomic unless explicitly guarded.\n"
                f"- Design defensively around boundary conditions.\n"
            )
            c.execute(
                "INSERT INTO lessons (module_id, title, content, completed, order_index) VALUES (?, ?, ?, ?, ?)",
                (mod_id, lesson_title, lesson_content, 0, l)
            )
            lesson_id = c.lastrowid

            # Add check-in quiz question
            c.execute(
                "INSERT INTO quizzes (lesson_id, question, options_json, correct_answer, explanation) VALUES (?, ?, ?, ?, ?)",
                (
                    lesson_id,
                    f"In {req.topic}, what is the primary operational trade-off when optimizing for throughput over latency?",
                    json.dumps([
                        "Batching requests increases throughput but adds individual request wait time",
                        "Throughput and latency always scale identically",
                        "Optimizing throughput reduces memory overhead to zero",
                        "Single-threaded execution guarantees the highest possible throughput"
                    ]),
                    "Batching requests increases throughput but adds individual request wait time",
                    "Throughput gains typically come from amortizing overhead across batches, which inherently delays individual request returns."
                )
            )

    conn.commit()
    conn.close()

    return CourseGenerateResponse(course_id=course_id, title=course_title)

@app.get("/api/courses", response_model=List[CourseSummary])
def list_courses():
    conn = sqlite3.connect(DB_FILE)
    c = conn.cursor()
    c.execute("SELECT id, title, topic, difficulty FROM courses ORDER BY id DESC")
    rows = c.fetchall()
    conn.close()
    return [CourseSummary(id=r[0], title=r[1], topic=r[2], difficulty=r[3]) for r in rows]

@app.get("/api/courses/{id}", response_model=CourseDetail)
def get_course(id: int):
    conn = sqlite3.connect(DB_FILE)
    c = conn.cursor()
    c.execute("SELECT id, title, topic, difficulty, description FROM courses WHERE id = ?", (id,))
    row = c.fetchone()
    if not row:
        conn.close()
        raise HTTPException(status_code=404, detail="Course not found")

    course_id, title, topic, difficulty, description = row

    c.execute("SELECT id, title, summary FROM modules WHERE course_id = ? ORDER BY order_index ASC", (id,))
    mod_rows = c.fetchall()

    modules = []
    for m in mod_rows:
        mod_id, m_title, m_summary = m
        c.execute("SELECT id, title, completed FROM lessons WHERE module_id = ? ORDER BY order_index ASC", (mod_id,))
        lesson_rows = c.fetchall()
        lessons = [LessonSummary(id=lr[0], title=lr[1], completed=bool(lr[2])) for lr in lesson_rows]
        modules.append(ModuleDetail(id=mod_id, title=m_title, summary=m_summary, lessons=lessons))

    conn.close()
    return CourseDetail(id=course_id, title=title, description=description, modules=modules)

@app.get("/api/lessons/{id}", response_model=LessonDetail)
def get_lesson(id: int):
    conn = sqlite3.connect(DB_FILE)
    c = conn.cursor()
    c.execute("SELECT id, title, content, completed FROM lessons WHERE id = ?", (id,))
    row = c.fetchone()
    if not row:
        conn.close()
        raise HTTPException(status_code=404, detail="Lesson not found")

    lesson_id, title, content, completed = row

    c.execute("SELECT id, question, options_json FROM quizzes WHERE lesson_id = ?", (lesson_id,))
    quiz_rows = c.fetchall()
    quiz = [
        QuizQuestionDto(id=qr[0], question=qr[1], options=json.loads(qr[2]))
        for qr in quiz_rows
    ]

    conn.close()
    return LessonDetail(id=lesson_id, title=title, content=content, completed=bool(completed), quiz=quiz)

@app.post("/api/lessons/{id}/complete", response_model=SimpleOk)
def complete_lesson(id: int):
    conn = sqlite3.connect(DB_FILE)
    c = conn.cursor()
    c.execute("UPDATE lessons SET completed = 1 WHERE id = ?", (id,))
    conn.commit()
    conn.close()
    return SimpleOk(ok=True)

@app.post("/api/quiz/answer", response_model=QuizAnswerResponse)
def answer_quiz(req: QuizAnswerRequest):
    conn = sqlite3.connect(DB_FILE)
    c = conn.cursor()
    c.execute("SELECT correct_answer, explanation FROM quizzes WHERE id = ?", (req.question_id,))
    row = c.fetchone()
    conn.close()

    if not row:
        raise HTTPException(status_code=404, detail="Question not found")

    correct_answer, explanation = row
    is_correct = (req.selected_answer.strip().lower() == correct_answer.strip().lower())

    return QuizAnswerResponse(
        correct=is_correct,
        correct_answer=correct_answer,
        explanation=explanation
    )
