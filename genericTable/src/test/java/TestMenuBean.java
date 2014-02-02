import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.karlNet.menu.MenuBean;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext.xml" })
public class TestMenuBean {
	@Autowired
	private MenuBean menuBean;
	
	@Test
	public void testInit() throws Exception {
		this.menuBean.init();
	}
}
