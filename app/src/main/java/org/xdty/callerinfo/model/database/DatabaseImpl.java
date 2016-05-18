package org.xdty.callerinfo.model.database;

import android.util.Log;

import org.xdty.callerinfo.model.db.Caller;
import org.xdty.callerinfo.model.db.InCall;
import org.xdty.callerinfo.model.db.MarkedRecord;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class DatabaseImpl implements Database {

    private static DatabaseImpl sDatabase;

    private DatabaseImpl() {

    }

    public static DatabaseImpl getInstance() {
        if (sDatabase == null) {
            sDatabase = new DatabaseImpl();
        }
        return sDatabase;
    }

    @Override
    public Observable<List<InCall>> fetchInCalls() {
        return Observable.create(new Observable.OnSubscribe<List<InCall>>() {
            @Override
            public void call(Subscriber<? super List<InCall>> subscriber) {
                subscriber.onNext(InCall.listAll(InCall.class, "time DESC"));
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<List<Caller>> fetchCallers() {
        return Observable.create(new Observable.OnSubscribe<List<Caller>>() {
            @Override
            public void call(Subscriber<? super List<Caller>> subscriber) {
                subscriber.onNext(Caller.listAll(Caller.class));
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void clearAllInCalls(List<InCall> inCallList) {
        Observable.from(inCallList).observeOn(Schedulers.io()).subscribe(
                new Action1<InCall>() {
                    @Override
                    public void call(InCall inCall) {
                        inCall.delete();
                    }
                });
    }

    @Override
    public void removeInCall(InCall inCall) {
        Observable.just(inCall).observeOn(Schedulers.io()).subscribe(new Action1<InCall>() {
            @Override
            public void call(InCall inCall) {
                inCall.delete();
            }
        });
    }

    @Override
    public Observable<Caller> findCaller(final String number) {
        return Observable.create(new Observable.OnSubscribe<Caller>() {
            @Override
            public void call(Subscriber<? super Caller> subscriber) {
                List<Caller> callers = Caller.find(Caller.class, "number=?", number);
                Caller caller = null;
                if (callers.size() > 0) {
                    caller = callers.get(0);
                }
                subscriber.onNext(caller);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void removeCaller(Caller caller) {
        Observable.just(caller).observeOn(Schedulers.io()).subscribe(new Action1<Caller>() {
            @Override
            public void call(Caller caller) {
                caller.delete();
            }
        });
    }

    @Override
    public void saveCaller(Caller caller) {
        Observable.just(caller).observeOn(Schedulers.io()).subscribe(new Action1<Caller>() {
            @Override
            public void call(Caller caller) {
                caller.save();
            }
        });
    }

    @Override
    public void saveInCall(InCall inCall) {
        Observable.just(inCall).observeOn(Schedulers.io()).subscribe(new Action1<InCall>() {
            @Override
            public void call(InCall inCall) {
                inCall.save();
            }
        });
    }

    @Override
    public void saveMarked(MarkedRecord markedRecord) {
        Observable.just(markedRecord)
                .observeOn(Schedulers.io())
                .subscribe(new Action1<MarkedRecord>() {
                    @Override
                    public void call(MarkedRecord markedRecord) {
                        markedRecord.save();
                    }
                });
    }

    @Override
    public void saveCaller(MarkedRecord markedRecord) {
        Observable.just(markedRecord)
                .observeOn(Schedulers.io())
                .subscribe(new Action1<MarkedRecord>() {
                    @Override
                    public void call(MarkedRecord markedRecord) {
                        Caller caller = new Caller();
                        caller.setNumber(markedRecord.getNumber());
                        caller.setName(markedRecord.getTypeName());
                        caller.setLastUpdate(markedRecord.getTime());
                        caller.setType("report");
                        caller.setOffline(false);
                        caller.save();
                    }
                });
    }

    @Override
    public Observable<List<MarkedRecord>> fetchMarkedRecords() {
        return Observable.create(new Observable.OnSubscribe<List<MarkedRecord>>() {
            @Override
            public void call(Subscriber<? super List<MarkedRecord>> subscriber) {
                subscriber.onNext(MarkedRecord.listAll(MarkedRecord.class));
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MarkedRecord> findMarkedRecord(final String number) {
        return Observable.create(new Observable.OnSubscribe<MarkedRecord>() {
            @Override
            public void call(Subscriber<? super MarkedRecord> subscriber) {
                List<MarkedRecord> records = MarkedRecord.find(MarkedRecord.class, "number=?",
                        number);
                MarkedRecord record = null;
                if (records.size() > 0) {
                    record = records.get(0);
                }
                subscriber.onNext(record);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void updateMarkedRecord(String number) {
        Observable.just(number)
                .observeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String number) {
                        List<MarkedRecord> records = MarkedRecord.find(MarkedRecord.class,
                                "number=?", number);
                        MarkedRecord record = null;
                        if (records.size() > 0) {
                            record = records.get(0);
                            record.setReported(true);
                            record.update();
                        }

                        if (records.size() > 1) {
                            Log.e("DatabaseImpl", "updateMarkedRecord duplicate number: " + number);
                        }
                    }
                });
    }
}