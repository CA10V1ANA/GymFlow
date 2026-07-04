-- Exercises catalog
CREATE TABLE exercises (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(150)        NOT NULL,
    image_url       VARCHAR(500),
    video_url       VARCHAR(500),
    category        VARCHAR(50)         NOT NULL, -- STRENGTH, CARDIO, MOBILITY, FUNCTIONAL...
    muscle_group    VARCHAR(50)         NOT NULL, -- CHEST, BACK, LEGS, SHOULDERS, ARMS, CORE, FULL_BODY
    equipment       VARCHAR(100),
    level           VARCHAR(20)         NOT NULL DEFAULT 'BEGINNER', -- BEGINNER, INTERMEDIATE, ADVANCED
    instructions    TEXT,
    created_at      TIMESTAMP           NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP           NOT NULL DEFAULT now()
);

CREATE INDEX idx_exercises_category ON exercises (category);
CREATE INDEX idx_exercises_muscle_group ON exercises (muscle_group);

-- Workouts (Treino A, B, C, D...)
CREATE TABLE workouts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id      UUID                NOT NULL REFERENCES students (id) ON DELETE CASCADE,
    instructor_id   UUID                REFERENCES users (id) ON DELETE SET NULL,
    name            VARCHAR(50)         NOT NULL, -- "Treino A"
    goal            VARCHAR(100),
    active          BOOLEAN             NOT NULL DEFAULT TRUE,
    start_date      DATE                NOT NULL DEFAULT CURRENT_DATE,
    end_date        DATE,
    notes           TEXT,
    created_at      TIMESTAMP           NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP           NOT NULL DEFAULT now()
);

CREATE INDEX idx_workouts_student_id ON workouts (student_id);

CREATE TABLE workout_exercises (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workout_id      UUID                NOT NULL REFERENCES workouts (id) ON DELETE CASCADE,
    exercise_id     UUID                NOT NULL REFERENCES exercises (id),
    sort_order      INTEGER             NOT NULL DEFAULT 0,
    sets            INTEGER             NOT NULL,
    repetitions     VARCHAR(20)         NOT NULL, -- "12" or "10-12"
    load_kg         NUMERIC(6,2),
    duration_seconds INTEGER,
    rest_seconds    INTEGER,
    notes           TEXT,
    created_at      TIMESTAMP           NOT NULL DEFAULT now()
);

CREATE INDEX idx_workout_exercises_workout_id ON workout_exercises (workout_id);
