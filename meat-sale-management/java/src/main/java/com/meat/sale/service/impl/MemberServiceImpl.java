package com.meat.sale.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meat.sale.entity.Member;
import com.meat.sale.mapper.MemberMapper;
import com.meat.sale.service.MemberService;
import org.springframework.stereotype.Service;
@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {}
