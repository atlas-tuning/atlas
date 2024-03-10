package com.github.manevolent.atlas.ui.behavior;

import java.util.ArrayList;
import java.util.List;

public abstract class History<T extends Action> {
    private final List<Action> actions = new ArrayList<>();
    private int maximumActions = 20;
    private int position = -1;

    private boolean remembering = true;

    public int getMaximumActions() {
        return maximumActions;
    }

    public void setMaximumActions(int maximumActions) {
        this.maximumActions = maximumActions;
    }

    public boolean isRemembering() {
        return remembering;
    }

    public void remember(T action) {
        if (!remembering) {
            return;
        }

        // Forget anything after the current position
        while (actions.size() - position > 1) {
            actions.removeLast();
        }
        int index = Math.max(0, Math.min(position + 1, actions.size()));
        actions.add(index, action);
        while (actions.size() > getMaximumActions()) {
            actions.removeFirst();
        }
        position = actions.size() - 1;
    }

    public boolean canUndo() {
        return position > 0;
    }

    public boolean canRedo() {
        return actions.size() - position > 1 && maximumActions - position > 1;
    }

    public void undo() {
        if (!canUndo()) {
            throw new IllegalStateException();
        }

        // Pop one
        boolean success = false;
        while (!success && canUndo()) {
            Action action = actions.get(position);
            remembering = false;
            try {
                success = action.undo();
            } finally {
                remembering = true;
            }
            position--;
        }

        position = Math.max(position, 0);
    }

    public void redo() {
        if (!canRedo()) {
            throw new IllegalStateException();
        }

        // Push forward one
        boolean success = false;
        while (!success && canRedo()) {
            Action action = actions.get(position + 1);
            remembering = false;
            try {
                success = action.redo();
            } finally {
                remembering = true;
            }
            position++;
        }

        position = Math.min(position, actions.size() - 1);
    }
}
