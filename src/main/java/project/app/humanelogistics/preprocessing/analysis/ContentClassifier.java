package project.app.humanelogistics.preprocessing.analysis;

import project.app.humanelogistics.model.DamageCategory;

public interface ContentClassifier {
    DamageCategory classify(String text);
}