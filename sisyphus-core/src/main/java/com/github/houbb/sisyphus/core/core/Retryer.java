package com.github.houbb.sisyphus.core.core;

import com.github.houbb.heaven.annotation.NotThreadSafe;
import com.github.houbb.heaven.support.instance.impl.InstanceFactory;
import com.github.houbb.sisyphus.api.support.block.RetryBlock;
import com.github.houbb.sisyphus.api.support.condition.RetryCondition;
import com.github.houbb.sisyphus.api.support.listen.RetryListen;
import com.github.houbb.sisyphus.api.support.recover.Recover;
import com.github.houbb.sisyphus.api.support.stop.RetryStop;
import com.github.houbb.sisyphus.api.support.wait.RetryWait;
import com.github.houbb.sisyphus.core.context.DefaultRetryContext;
import com.github.houbb.sisyphus.core.support.block.ThreadSleepRetryBlock;
import com.github.houbb.sisyphus.core.support.condition.AlwaysFalseRetryCondition;
import com.github.houbb.sisyphus.core.support.listen.NoRetryListen;
import com.github.houbb.sisyphus.core.support.recover.NoRecover;
import com.github.houbb.sisyphus.core.support.stop.MaxAttemptRetryStop;
import com.github.houbb.sisyphus.core.support.wait.NoRetryWait;

import java.util.concurrent.Callable;

/**
 * 引导类入口
 *
 * @author binbin.hou
 * @since 1.0.0
 */
@NotThreadSafe
public class Retryer<R> {

    /**
     * 执行重试的条件
     * 1. 默认不进行重试
     * 2. 支持多个条件，任意一个满足则满足。如果用户有更特殊的需求，应该自己定义。
     */
    private RetryCondition condition = InstanceFactory.getInstance().singleton(AlwaysFalseRetryCondition.class);

    /**
     * 等待的策略
     * 1. 默认不进行等待
     * 2. 支持多个等待策略混合。将所有的混合策略时间加在一起。
     */
    private RetryWait waits =  InstanceFactory.getInstance().singleton(NoRetryWait.class);

    /**
     * 阻塞的方式
     * 1. 默认采用线程沉睡的方式
     */
    private RetryBlock block = InstanceFactory.getInstance().singleton(ThreadSleepRetryBlock.class);

    /**
     * 停止的策略
     * 1. 默认重试3次
     * 2. 暂时不进行暴露自定义。因为实际生产中重试次数是最实用的一个策略。
     */
    private RetryStop stop = new MaxAttemptRetryStop(3);

    /**
     * 监听器
     * 1. 默认不进行任何操作
     */
    private RetryListen listen = InstanceFactory.getInstance().singleton(NoRetryListen.class);

    /**
     * 恢复策略
     * 1. 默认不进行任何操作
     */
    private Recover recover = InstanceFactory.getInstance().singleton(NoRecover.class);

    /**
     * 创建实例化对象
     * @param <R> 泛型
     * @return 结果
     */
    public static <R> Retryer<R> newInstance() {
        return new Retryer<>();
    }

    /**
     * 重试生效条件
     *
     * @param condition 生效条件
     * @return this
     */
    public Retryer<R> condition(RetryCondition condition) {
        this.condition = condition;
        return this;
    }

    /**
     * 最大等待策略
     *
     * @param waits 等待策略
     * @return this
     */
    public Retryer<R> waits(RetryWait waits) {
        this.waits = waits;
        return this;
    }

    /**
     * 最大尝试次数
     *
     * @param maxAttempt 最大尝试次数
     * @return this
     */
    public Retryer<R> maxAttempt(final int maxAttempt) {
        this.stop = new MaxAttemptRetryStop(maxAttempt);
        return this;
    }

    /**
     * 设置阻塞策略
     *
     * @param block 阻塞策略
     * @return this
     */
    private Retryer<R> block(RetryBlock block) {
        this.block = block;
        return this;
    }

    /**
     * 设置停止策略
     *
     * @param stop 停止策略
     * @return this
     */
    private Retryer<R> stop(RetryStop stop) {
        this.stop = stop;
        return this;
    }

    /**
     * 设置监听
     *
     * @param listen 监听
     * @return this
     */
    public Retryer<R> listen(RetryListen listen) {
        this.listen = listen;
        return this;
    }

    /**
     * 设置恢复策略
     *
     * @param recover 恢复策略
     * @return this
     */
    public Retryer<R> recover(Recover recover) {
        this.recover = recover;
        return this;
    }

    /**
     * 执行重试
     *
     * @param callable 可执行的方法
     * @return 执行的结果
     */
    public R retry(Callable<R> callable) {
        // 初始化
        DefaultRetryContext<R> context = new DefaultRetryContext<>();
        context.callable(callable)
                .block(block)
                .stop(stop)
                .condition(condition)
                .waits(waits)
                .listen(listen)
                .recover(recover);

        // 调用执行结果
        DefaultRetry<R> defaultRetry = new DefaultRetry<>();
        return defaultRetry.retry(context);
    }

}
