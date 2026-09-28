import os
from datetime import datetime, timezone
from sqlalchemy import create_engine, String, Integer, DateTime, JSON, ForeignKey
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column, sessionmaker

class Base(DeclarativeBase):
    pass

class SearchJob(Base):
    __tablename__ = 'search_jobs'
    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    query: Mapped[str] = mapped_column(String(240))
    mode: Mapped[str] = mapped_column(String(20))
    engines: Mapped[list] = mapped_column(JSON)
    crawl: Mapped[int] = mapped_column(Integer,default=0)
    status: Mapped[str] = mapped_column(String(20), default='queued')
    round: Mapped[int] = mapped_column(Integer, default=0)
    duplicates: Mapped[int] = mapped_column(Integer, default=0)
    filtered: Mapped[int] = mapped_column(Integer, default=0)
    errors: Mapped[list] = mapped_column(JSON, default=list)
    created: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))

class SearchResult(Base):
    __tablename__ = 'search_results'
    job_id: Mapped[str] = mapped_column(ForeignKey('search_jobs.id',ondelete='CASCADE'),primary_key=True)
    key: Mapped[str] = mapped_column(String(64), primary_key=True)
    payload: Mapped[dict] = mapped_column(JSON)

engine = create_engine(os.getenv('DATABASE_URL','postgresql+psycopg://searchhh:searchhh@localhost/searchhh'),pool_pre_ping=True)
Session = sessionmaker(engine,expire_on_commit=False)
