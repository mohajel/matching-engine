package ir.ramtung.tinyme.domain.service;

public abstract class CommandProcessor {
    protected final MatchingControlList controls;

    public CommandProcessor(MatchingControlList controls) {
        this.controls = controls;
    }

    public MatchResult processCommand() {
        start();
        MatchResult result = process();
        if (result.isFailure()) {
            controls.abort();
            return result;
        }
        MatchingOutcome finishOutcome = controls.finish(result);
        if (!finishOutcome.equals(MatchingOutcome.OK)) {
            undo(result);
            return new MatchResult(finishOutcome);
        }
        return result;
    }

    protected abstract void start();
    protected abstract MatchResult process();
    protected abstract void undo(MatchResult result);
}
