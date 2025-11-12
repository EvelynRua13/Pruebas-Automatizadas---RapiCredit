package com.rapicredit.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.targets.Target;

public class ClickIfPresent implements Interaction {
    private final Target target;

    public ClickIfPresent(Target target) {
        this.target = target;
    }

    public static ClickIfPresent on(Target target) {
        return new ClickIfPresent(target);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        try {
            if (target.resolveFor(actor).isCurrentlyVisible() && target.resolveFor(actor).isEnabled()) {
                target.resolveFor(actor).click();
            }
        } catch (Exception e) {
            // element not found or not clickable - ignore silently
        }
    }
}
