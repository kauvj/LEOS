/*
 * Copyright 2019 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.annotate;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.europa.ec.leos.annotate.helper.SpotBugsAnnotations;
import eu.europa.ec.leos.annotate.helper.TestData;
import eu.europa.ec.leos.annotate.helper.TestDbHelper;
import eu.europa.ec.leos.annotate.model.SimpleMetadata;
import eu.europa.ec.leos.annotate.model.UserDetails;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.*;
import eu.europa.ec.leos.annotate.model.entity.Annotation.AnnotationStatus;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotation;
import eu.europa.ec.leos.annotate.repository.*;
import eu.europa.ec.leos.annotate.services.AnnotationConversionService;
import eu.europa.ec.leos.annotate.services.AnnotationService;
import eu.europa.ec.leos.annotate.services.exceptions.CannotCreateAnnotationException;
import eu.europa.ec.leos.annotate.services.exceptions.MissingPermissionException;
import eu.europa.ec.leos.annotate.services.impl.UserDetailsCache;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.config.name=anot")
@ActiveProfiles("test")
public class AnnotationRetrievalTest {

    private Group defaultGroup;

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------
    @Autowired
    private AnnotationService annotService;

    @Autowired
    private AnnotationConversionService conversionService;
    
        @Autowired
    @Qualifier("annotationTestRepos")
    private AnnotationTestRepository annotRepos;
    
    @Autowired
    private GroupRepository groupRepos;

    @Autowired
    private UserRepository userRepos;

    @Autowired
    private MetadataRepository metadataRepos;

    @Autowired
    private UserGroupRepository userGroupRepos;

    @Autowired
    private UserDetailsCache userDetailsCache;

    private final static String LOGIN = "demo";
    private final static String HYPO_PREFIX = "acct:user@";
    
    // -------------------------------------
    // Cleanup of database content
    // -------------------------------------

    @Before
    public void cleanDatabaseBeforeTests() {

        TestDbHelper.cleanupRepositories(this);
        userDetailsCache.clear();
    }

    @After
    public void cleanDatabaseAfterTests() {

        TestDbHelper.cleanupRepositories(this);
        userDetailsCache.clear();
    }

    // -------------------------------------
    // Tests
    // -------------------------------------

    /**
     * let an annotation be created by the service, and verify if can be found again by the same user
     * convert it to JSON format and verify user detail properties
     */
    @Test
    public void testFindAnnotation() throws MissingPermissionException {

        // done here instead of in tests before-execution function to avoid SpotBugs complaint and to avoid suppressing SpotBugs warning
        defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);

        final String authority = "auth";
        final String hypothesisUserAccount = "acct:" + LOGIN + "@" + authority;
        final String Key_SomeProp = "responseVersion";
        final String Value_SomeProp = "1";
        final String Key_RespStatusProp = "responseStatus";
        final String Value_RespStatusProp = "IN_PREPARATION";
        final String Key_SystemId = "systemId";

        final User user = new User(LOGIN);
        userRepos.save(user);

        userDetailsCache.cache(LOGIN, new UserDetails(LOGIN, Long.valueOf(4), "first", "last", "entity", LOGIN + "@" + authority, null));

        final UserInformation userInfo = new UserInformation(user, authority);

        // let the annotation be created - with some metadata
        JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(hypothesisUserAccount);
        jsAnnot.getDocument().getMetadata().put(Key_SomeProp, Value_SomeProp);
        jsAnnot.getDocument().getMetadata().put(Key_RespStatusProp, Value_RespStatusProp);
        try {
            jsAnnot = annotService.createAnnotation(jsAnnot, userInfo);
        } catch (CannotCreateAnnotationException e) {
            Assert.fail("Unexpected exception received: " + e);
        }
        Assert.assertNotNull(jsAnnot);

        // verification: search the annotation again based on its ID with the same user
        final Annotation ann = annotService.findAnnotationById(jsAnnot.getId(), LOGIN);
        Assert.assertNotNull(ann);

        final JsonAnnotation jsAnnotOut = conversionService.convertToJsonAnnotation(ann, userInfo);
        Assert.assertNotNull(jsAnnotOut);
        Assert.assertNotNull(jsAnnotOut.getUser_info());
        Assert.assertEquals("last first", jsAnnotOut.getUser_info().getDisplay_name());
        Assert.assertEquals("entity", jsAnnotOut.getUser_info().getEntity_name());

        // verify that all metadata is properly returned (systemId, responseStatus, and other key-value items)
        final SimpleMetadata metadataOut = jsAnnotOut.getDocument().getMetadata();
        Assert.assertEquals(Value_SomeProp, metadataOut.get(Key_SomeProp));
        Assert.assertEquals(Value_RespStatusProp, metadataOut.get(Key_RespStatusProp));
        Assert.assertEquals(authority, metadataOut.get(Key_SystemId));
        
        // verify that it has NORMAL status and was not updated yet
        Assert.assertNotNull(jsAnnotOut.getStatus());
        Assert.assertEquals(AnnotationStatus.NORMAL, jsAnnotOut.getStatus().getStatus());
        Assert.assertNull(jsAnnotOut.getStatus().getUpdated());
        Assert.assertNull(jsAnnotOut.getStatus().getUpdated_by());
    }

    /**
     * search for an annotation by using empty ID, and for non-existing annotation
     */
    @Test
    @SuppressFBWarnings(value = SpotBugsAnnotations.ExceptionIgnored, justification = SpotBugsAnnotations.ExceptionIgnored)
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public void testFindNonExistingAnnotation() throws CannotCreateAnnotationException, MissingPermissionException {

        // done here instead of in tests before-execution function to avoid SpotBugs complaint and to avoid suppressing SpotBugs warning
        defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);

        final String authority = Authorities.EdiT;
        final String hypothesisUserAccount = "acct:" + LOGIN + "@" + authority;
        final User user = new User(LOGIN);
        userRepos.save(user);

        final UserInformation userInfo = new UserInformation(user, authority);

        // let the annotation be created
        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(hypothesisUserAccount);
        annotService.createAnnotation(jsAnnot, userInfo);

        // verification: search the annotation again based on its ID with the same user
        try {
            annotService.findAnnotationById(null, LOGIN);
            Assert.fail("Expected exception for missing annotation ID not received");
        } catch (Exception e) {
            // OK
        }

        // search for non-existing ID
        Assert.assertNull(annotService.findAnnotationById("myid", LOGIN));
    }

    /**
     * let a private annotation be created by the service, and different user wants to see it
     * -> not shown
     */
    @Test(expected = MissingPermissionException.class)
    public void testFindPrivateAnnotationOfOtherUser() throws MissingPermissionException, CannotCreateAnnotationException {

        // done here instead of in tests before-execution function to avoid SpotBugs complaint and to avoid suppressing SpotBugs warning
        defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);

        // create two users and assign them to the default group
        final String authority = "europa";
        final String otherLogin = "demo2";
        final String hypothesisUserAccount = HYPO_PREFIX + authority;

        final User theUser = userRepos.save(new User(LOGIN));
        final User secondUser = userRepos.save(new User(otherLogin));
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));
        userGroupRepos.save(new UserGroup(secondUser.getId(), defaultGroup.getId()));

        final UserInformation userInfo = new UserInformation(theUser, authority);

        // let the annotation be created - private for the first user
        JsonAnnotation jsAnnot = TestData.getTestPrivateAnnotationObject(hypothesisUserAccount);
        jsAnnot = annotService.createAnnotation(jsAnnot, userInfo);
        Assert.assertNotNull(jsAnnot);

        // verification: search the annotation again based on its ID and from another user
        // -> as it is private, the other user may not see it
        annotService.findAnnotationById(jsAnnot.getId(), otherLogin); // should throw MissingPermissionException
    }

    /**
     * let a public annotation be created, try retrieving it by a different user of same group
     * --> is valid
     */
    @Test
    public void testFindPublicAnnotationOfOtherUser() throws MissingPermissionException, CannotCreateAnnotationException {

        // done here instead of in tests before-execution function to avoid SpotBugs complaint and to avoid suppressing SpotBugs warning
        defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);

        // create two users and assign them to the default group
        final String authority = "eu";
        final String otherLogin = "demo2";
        final String hypothesisUserAccount = HYPO_PREFIX + authority;

        final User theUser = userRepos.save(new User(LOGIN));
        final User secondUser = userRepos.save(new User(otherLogin));
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));
        userGroupRepos.save(new UserGroup(secondUser.getId(), defaultGroup.getId()));

        final UserInformation userInfo = new UserInformation(theUser, authority);

        // let the public annotation be created
        JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(hypothesisUserAccount);
        jsAnnot = annotService.createAnnotation(jsAnnot, userInfo);
        Assert.assertNotNull(jsAnnot);

        // verification: search the annotation again based on its ID and from another user
        // -> as it is public, the other user may see it
        final Annotation annot = annotService.findAnnotationById(jsAnnot.getId(), otherLogin);
        Assert.assertNotNull(annot);
    }

    /**
     * let a public annotation be created; different user not belonging to the group tries seeing it
     * -> not shown
     */
    @Test(expected = MissingPermissionException.class)
    public void testFindPublicAnnotationOfOtherUserNotInGroup() throws MissingPermissionException, CannotCreateAnnotationException {

        // done here instead of in tests before-execution function to avoid SpotBugs complaint and to avoid suppressing SpotBugs warning
        defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);

        // create two users and assign only one of them to the default group
        final String authority = "europa";
        final String otherLogin = "demo2";
        final String hypothesisUserAccount = HYPO_PREFIX + authority;
        final User theUser = userRepos.save(new User(LOGIN));
        userRepos.save(new User(otherLogin));
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));

        final UserInformation userInfo = new UserInformation(theUser, authority);

        // let the public annotation be created
        JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(hypothesisUserAccount);
        jsAnnot = annotService.createAnnotation(jsAnnot, userInfo);
        Assert.assertNotNull(jsAnnot);

        // verification: search the annotation again based on its ID and from another user
        // -> as it is public, the other user may theoretically see it, but he is not a group member
        annotService.findAnnotationById(jsAnnot.getId(), otherLogin); // should throw MissingPermissionException
    }

    /**
     * test retrieval of all annotation IDs associated to certain metadata
     */
    @Test
    public void testFindAnnotationsOfMetadata() throws Exception {

        defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);

        final String authority = "europa";
        final String hypothesisUserAccount = HYPO_PREFIX + authority;
        final User theUser = userRepos.save(new User(LOGIN));
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));

        final UserInformation userInfo = new UserInformation(theUser, authority);

        // let four annotations be created - three associated to same metadata (but one is deleted)
        JsonAnnotation jsAnnot1 = TestData.getTestAnnotationObject(hypothesisUserAccount);
        jsAnnot1 = annotService.createAnnotation(jsAnnot1, userInfo);

        JsonAnnotation jsAnnot2 = TestData.getTestAnnotationObject(hypothesisUserAccount);
        jsAnnot2 = annotService.createAnnotation(jsAnnot2, userInfo);
        
        // third annotation, but is deleted already
        JsonAnnotation jsAnnot3 = TestData.getTestAnnotationObject(hypothesisUserAccount);
        jsAnnot3 = annotService.createAnnotation(jsAnnot3, userInfo);
        final Annotation readAnnot = annotRepos.findById(jsAnnot3.getId());
        readAnnot.setStatus(AnnotationStatus.DELETED);
        annotRepos.save(readAnnot);

        // fourth annotation, associated to different metadata
        JsonAnnotation jsAnnot4 = TestData.getTestAnnotationObject(hypothesisUserAccount);
        jsAnnot4.getDocument().getMetadata().put("ISCReference", "ISC/1/2"); // other metadata
        jsAnnot4 = annotService.createAnnotation(jsAnnot4, userInfo);

        Assert.assertEquals(2, metadataRepos.count());

        final Metadata metaWithTwoAnnots = annotService.findAnnotationById(jsAnnot1.getId()).getMetadata();
        final Metadata metaWithOneAnnot = annotService.findAnnotationById(jsAnnot4.getId()).getMetadata();

        // verify that the service returns the correct annotation items 
        // and number of items: two only (third is not counted as it is deleted)
        final List<String> twoIds = annotService.getAnnotationIdsOfMetadata(Arrays.asList(metaWithTwoAnnots.getId()));
        Assert.assertEquals(2, twoIds.size());
        Assert.assertTrue(twoIds.contains(jsAnnot1.getId()));
        Assert.assertTrue(twoIds.contains(jsAnnot2.getId()));
        Assert.assertFalse(twoIds.contains(jsAnnot3.getId()));

        final List<String> oneId = annotService.getAnnotationIdsOfMetadata(Arrays.asList(metaWithOneAnnot.getId()));
        Assert.assertEquals(1, oneId.size());
        Assert.assertTrue(oneId.contains(jsAnnot4.getId()));

        // random id should not contain any items
        Assert.assertEquals(0, annotService.getAnnotationIdsOfMetadata(Arrays.asList(Long.MAX_VALUE)).size());
        
        // finally check that all three non-deleted annotations are returned when asking for both metadata sets
        final List<String> threeIds = annotService.getAnnotationIdsOfMetadata(Arrays.asList(metaWithOneAnnot.getId(), metaWithTwoAnnots.getId()));
        Assert.assertEquals(3, threeIds.size());
        Assert.assertTrue(threeIds.contains(jsAnnot1.getId()));
        Assert.assertTrue(threeIds.contains(jsAnnot2.getId()));
        Assert.assertFalse(threeIds.contains(jsAnnot3.getId())); // the deleted one
        Assert.assertTrue(threeIds.contains(jsAnnot4.getId()));
    }
}
