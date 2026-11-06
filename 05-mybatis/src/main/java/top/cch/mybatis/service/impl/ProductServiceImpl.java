package top.cch.mybatis.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.cch.mybatis.entity.Product;
import top.cch.mybatis.mapper.ProductMapper;
import top.cch.mybatis.service.ProductService;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
}
