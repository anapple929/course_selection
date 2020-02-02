package com.course_selection.realm;

import com.course_selection.mapper.StudentMapper;
import com.course_selection.pojo.Student;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 用户自定义的realm用于shiro的认证、授权
 */
public class CustomRealm extends AuthorizingRealm {

    private static final Logger log= LoggerFactory.getLogger(CustomRealm.class);

    private static final Long sessionKeyTimeOut=3600_000L;

    @Autowired
    private StudentMapper studentMapper;
    /**
     * 授权
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken token=(UsernamePasswordToken) authenticationToken;
        String userNumber=token.getUsername();
        String password =String.valueOf(token.getPassword());
        log.info("当前登陆的学号={}， 密码={}",userNumber,password);
        Integer sid=Integer.parseInt(userNumber);
        Student student=studentMapper.findBySid(sid);
        if (student==null){
            throw new UnknownAccountException("用户名不存在!");
        }
        if (!student.getPassword().equals(password)){
            throw new IncorrectCredentialsException("用户名密码不匹配!");
        }
        SimpleAuthenticationInfo info=new SimpleAuthenticationInfo(student.getSid(),password,getName());
        setSession("id",student.getId());
        return info;
    }
    /**
     * 将key与对应的value塞入shiro的session中-最终交给HttpSession进行管理(如果是分布式session配置，那么就是交给redis管理)
     * @param key
     * @param value
     */
    private void setSession(String key,Object value){
        Session session= SecurityUtils.getSubject().getSession();
        if (session!=null){
            session.setAttribute(key,value);
            session.setTimeout(sessionKeyTimeOut);//设置时间
        }
    }
}
