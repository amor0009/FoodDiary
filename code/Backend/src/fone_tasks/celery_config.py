from celery import Celery
from celery.schedules import crontab

celery = Celery(
    __name__,
    broker="redis://localhost:6379/0",
    backend="redis://localhost:6379/0"
)

celery.conf.update(
    task_serializer="json",
    accept_content=["json"],
    result_serializer="json",
    timezone="UTC",
    enable_utc=True,
)

celery.conf.beat_schedule = {
    'delete-old-user-weights-every-day': {
        'task': 'tasks.delete_old_user_weights',  # имя задачи из tasks.py
        'schedule': crontab(hour=0, minute=0),    # запуск каждый день в полночь
    },
    'delete-old-meal-products-every-day': {
        'task': 'tasks.delete_old_meal_products',
        'schedule': crontab(hour=0, minute=0),    # запуск каждый день в полночь
    },
}
