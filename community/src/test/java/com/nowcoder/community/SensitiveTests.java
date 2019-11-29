package com.nowcoder.community;

import com.nowcoder.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTests {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter() {

        String text = "范·达克霍姆（Van Darkholme，1972年10月24日-），越南裔美国gay片演员，制作人，行为艺术家。出演过刘door华的《大冒险家》" +
                "哲♂学界人称van样(vanSama)，搓屌电音创始人，擅长fa乐器，有最美不过fa乐器的美称";
        System.out.println(sensitiveFilter.filter(text));
    }
}
