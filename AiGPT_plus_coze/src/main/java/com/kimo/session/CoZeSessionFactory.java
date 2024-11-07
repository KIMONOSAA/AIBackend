

package com.kimo.session;



public interface CoZeSessionFactory {

    /**
     * 开启会话
     *
     * @return {@link CoZeSession}
     */
    CoZeSession openSession();

}
