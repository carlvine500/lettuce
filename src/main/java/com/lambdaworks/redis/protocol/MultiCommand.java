// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.redis.protocol;

import com.google.common.util.concurrent.AbstractFuture;
import com.lambdaworks.redis.RedisCommandInterruptedException;
import io.netty.buffer.ByteBuf;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A redis command and its result. All successfully executed commands will eventually return a {@link com.lambdaworks.redis.protocol.CommandOutput} object.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 * @param <T> Command output type.
 *
 * @author Will Glozer
 */
public class MultiCommand<K, V, T> extends AbstractFuture<T> implements RedisCommand<K, V, T> {

    private RedisCommand<K, V, T> command;

    private boolean receivedQueued = false;
    public MultiCommand(RedisCommand<K,V,T> actualCommand) {
        this.command=actualCommand;
    }

    /**
     * Check if the command has been cancelled.
     *
     * @return True if the command was cancelled.
     */
    @Override
    public boolean isCancelled() {
        return command.isCancelled();
    }

    /**
     * Check if the command has completed.
     *
     * @return true if the command has completed.
     */
    @Override
    public boolean isDone() {
        return command.isDone();
    }

    /**
     * Get the command output and if the command hasn't completed yet, wait until it does.
     *
     * @return The command output.
     */
    @Override
    public T get() throws ExecutionException {
        try {
            return command.get();
        } catch (InterruptedException e) {
            throw new RedisCommandInterruptedException(e);
        }
    }

    /**
     * Get the command output and if the command hasn't completed yet, wait up to the specified time until it does.
     *
     * @param timeout Maximum time to wait for a result.
     * @param unit Unit of time for the timeout.
     *
     * @return The command output.
     *
     * @throws java.util.concurrent.TimeoutException if the wait timed out.
     */
    @Override
    public T get(long timeout, TimeUnit unit) throws TimeoutException, ExecutionException {
        try {
            return command.get(timeout,unit);
        } catch (InterruptedException e) {
            throw new RedisCommandInterruptedException(e);
        }
    }

    @Override public String getError() {
        return command.getError();
    }

    /**
     * Wait up to the specified time for the command output to become available.
     * 
     * @param timeout Maximum time to wait for a result.
     * @param unit Unit of time for the timeout.
     * 
     * @return true if the output became available.
     */
    public boolean await(long timeout, TimeUnit unit) {
        return command.await(timeout, unit);
    }

    /**
     * Get the object that holds this command's output.
     * 
     * @return The command output object.
     */
    @Override
    public CommandOutput<K, V, T> getOutput() {
        return command.getOutput();
    }

    /**
     * Mark this command complete and notify all waiting threads.
     */
    @Override
    public void complete() {
        if(receivedQueued){
            command.complete();
        }else{
            receivedQueued=true;
        }
    }

    @Override public CommandArgs<K, V> getArgs() {
        return command.getArgs();
    }

    /**
     * Encode and write this command to the supplied buffer using the new <a href="http://redis.io/topics/protocol">Unified
     * Request Protocol</a>.
     * 
     * @param buf Buffer to write to.
     */
    public void encode(ByteBuf buf) {
        command.encode(buf);
    }



    public boolean setException(Throwable exception) {
        command.setException(exception);
        return true;
    }

}
