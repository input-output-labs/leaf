# LEAF

Leaf is a **Spring based** library that offers conveniant turnkey  **Services** for Web App backends.
The main features are:
 - [x] Account/User management
 - [x] Administration management
 - [x] Whitelisting
 - [x] Email sending services
 - [ ] Content Management Service
 - [ ] Account/User notifications

## Quickstart

Import the releases repository:

 	<repositories>
		<repository>
		  <id>io-labs-snapshots</id>
		  <url>https://io-labs.fr/nexus/repository/maven-snapshots/</url>
		</repository>
	</repositories>

Import the dependency:

	<dependency>
		<groupId>fr.io-labs</groupId>
		<artifactId>leaf</artifactId>
		<version>0.0.2-SNAPSHOT</version>
	</dependency>

Annotate your main application class:

    @SpringBootApplication
	@ComponentScan(basePackages = { "fr.iolabs.leaf", <your own main package here> })
	@EnableMongoRepositories(basePackages = { "fr.iolabs.leaf", <your own main package here> })

## Main features

### Account/User management
#### Extending the Leaf default Account

	public class MyAccount extends LeafAccount {

    private String name;

    public Account() {
        super();
        this.name = "default name";
    }

Your class will be discovered and used by Leaf automatically.
Make sure to have a default constructor which is calling the super() constructor.

#### Exposing the default Leaf API

    @RestController
	@RequestMapping("/api")
	public class AccountController extends LeafAccountController<MyAccount> {
	}

You can of course add your own methods to this controller.

#### Injecting the Service or the Repository in your components

    @Autowired
    private LeafAccountService<MyAccount> leafAccountService;

    @Autowired
    private LeafAccountRepository<MyAccount> leafAccountRepository;

### Administration management
#### Exposing the default Leaf API

    @RestController
	@RequestMapping("/api")
	public class AdminController extends LeafAdminController<MyAccount> {
	}

You can of course add your own methods to this controller.

#### Injecting the Service in your components

    @Autowired
    private LeafAminService<MyAccount> leafAccountService;

#### Enabling whitelisting

Whitelisting allows administrators to set a list of accepted email.
If an user try to register but his email is not in the whitelist, then the account creation is blocked.

To enable whitelisting add the following in application.properties:

	leaf.whitelisting.enabled=true

### Email sending service
Emailling his using a third party SAAS API named MailGun.
Documentation can be found here: https://www.mailgun.com/

#### Injecting the Service in your components

    @Autowired
    private LeafEmailService leafEmailService;

#### Configuring emailling
Add the following in application.properties:

    mailgun.api.url=<your mailgun api>/messages
	mailgun.api.from=<your sender email>
	mailgun.api.key=<your mailgun api key>
