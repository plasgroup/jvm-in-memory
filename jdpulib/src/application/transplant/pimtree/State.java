package application.transplant.pimtree;

public enum State {
    idle,
    pre_init,
    loading_tasks,
    loading_finished,
    waiting_for_sync,
    supplying_responces
};