package im.eg.heepay.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import im.eg.heepay.mapper.UserAccountMapper;
import im.eg.heepay.mapper.UserBindMapper;
import im.eg.heepay.model.UserAccount;
import im.eg.heepay.model.UserBind;
import im.eg.heepay.service.UserBindService;
import im.eg.heepay.util.HfbException;
import im.eg.heepay.util.ResultCodeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;
import java.util.UUID;

@Service
public class UserBindServiceImpl extends ServiceImpl<UserBindMapper, UserBind> implements UserBindService {

	@Resource
	private UserAccountMapper userAccountMapper;

	@Transactional(rollbackFor = Exception.class)
	@Override
	public UserBind bind(Map<String, Object> paramMap) {
		UserBind userBind = JSON.parseObject(JSON.toJSONString(paramMap), UserBind.class);
		String bindCode = UUID.randomUUID().toString().replaceAll("-", "");
		userBind.setBindCode(bindCode);
		baseMapper.insert(userBind);

		UserAccount userAccount =  new UserAccount();
		userAccount.setUserCode(bindCode);
		userAccount.setAmount("0");
		userAccount.setFreezeAmount("0");
		userAccountMapper.insert(userAccount);
		return userBind;
	}

	@Override
	public boolean isBind(String idCard) {
		Integer count = baseMapper.selectCount(new QueryWrapper<UserBind>().eq("id_card", idCard));
		if(count.intValue() > 0) {
			return true;
		}
		return false;
	}

	@Override
	public UserBind getByBindCode(String bindCode) {
		return baseMapper.selectOne(new QueryWrapper<UserBind>().eq("bind_code", bindCode));
	}

	@Override
	public void checkPassword(String bindCode, String passwd) {
		if(StringUtils.isEmpty(passwd)) {
			throw new HfbException(ResultCodeEnum.PAY_PASSWORD_ERROR);
		}
		UserBind userBind = this.getByBindCode(bindCode);
		if(!passwd.equals(userBind.getPayPasswd())) {
			throw new HfbException(ResultCodeEnum.PAY_PASSWORD_ERROR);
		}
	}
}
