package com.example.truytimkhobaubtl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuizManager {
    private List<Quiz> quizzes;

    public QuizManager() {
        quizzes = new ArrayList<>();
        quizzes.add(new Quiz("1 + 1 = ?", "2"));
        quizzes.add(new Quiz("2 + 2 = ?", "4"));
        quizzes.add(new Quiz("2 + 5 = ?", "7"));
        quizzes.add(new Quiz("11 + 2 = ?", "13"));
        // Thêm câu đố khác nếu cần
    }

    public Quiz getRandomQuiz() {
        Random random = new Random();
        return quizzes.get(random.nextInt(quizzes.size()));
    }

    public boolean checkAnswer(Quiz quiz, String answer) {
        return quiz.getAnswer().equalsIgnoreCase(answer.trim());
    }

    public static class Quiz {
        private String question;
        private String answer;

        public Quiz(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }
    }
}
